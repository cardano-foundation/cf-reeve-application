package org.cardanofoundation.lob.app.netsuite_adapter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress;
import org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NetSuiteAPI netSuiteAPI;

    private final ObjectMapper objectMapper;

    private final TransactionConverter transactionConverter;

    // TODO this this over properly
    @Value("${lob.connector.id:jhu765}")
    private String connectorId;

    @Value("${lob.netsuite.send.batch.size:25}")
    private int sendBatchSize = 25;

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestion> findIngestionById(UUID id) {
        return ingestionRepository.findById(id);
    }

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines() {
        return netSuiteAPI.retrieveLatestNetsuiteTransactionLines();
    }

    @Transactional
    public void startERPExtraction(String initiator,
                                   FilteringParameters filteringParameters) {
        val netsuiteTransactionLinesJsonE = retrieveAndStoreNetSuiteIngestion();

        if (netsuiteTransactionLinesJsonE.isEmpty()) {
            log.warn("Error retrieving data from NetSuite API: {}", netsuiteTransactionLinesJsonE.getLeft().getDetail());
            return;
        }

        val netsuiteIngestion = netsuiteTransactionLinesJsonE.get();
        val netsuiteTransactionLinesJson = netsuiteIngestion.getIngestionBody();
        val transactionDataSearchResultE = parseNetSuiteSearchResults(netsuiteTransactionLinesJson);

        if (transactionDataSearchResultE.isEmpty()) {
            log.warn("Error parsing NetSuite search result: {}", transactionDataSearchResultE.getLeft().getDetail());
            return;
        }

        val transactionDataSearchResult = transactionDataSearchResultE.get();

        val coreTransactionsE = transactionConverter.convert(transactionDataSearchResult);
        if (coreTransactionsE.isEmpty()) {
            val issue = coreTransactionsE.getLeft();

            applicationEventPublisher.publishEvent(NotificationEvent.create(NotificationSeverity.ERROR, issue));

            log.warn("Error converting NetSuite search result: {}", coreTransactionsE.getLeft().getDetail());
            return;
        }
        val coreTransactionsToOrganisationMap = coreTransactionsE.get();

        log.info("CoreTransactionLines count: {}", coreTransactionsToOrganisationMap.size());

        coreTransactionsToOrganisationMap.forEach((organisationId, coreTransactions) -> {
            val filteredCoreTransactions = coreTransactions.stream()
                    .filter(line -> filteringParameters.getFrom().isEmpty() || filteringParameters.getFrom().map(date -> line.getEntryDate().isAfter(date)).orElse(false))
                    .filter(line -> filteringParameters.getTo().isEmpty() || filteringParameters.getTo().map(date -> line.getEntryDate().isBefore(date)).orElse(false))
                    .filter(line -> filteringParameters.getTransactionTypes().isEmpty() || filteringParameters.getTransactionTypes().contains(line.getTransactionType()))
                    .filter(line -> filteringParameters.getOrganisationIds().isEmpty() || filteringParameters.getOrganisationIds().contains(line.getOrganisation().getId()))
                    .filter(line -> filteringParameters.getProjectInternalNumbers().isEmpty() || line.getProjectInternalNumber().map(projectCode -> filteringParameters.getProjectInternalNumbers().contains(projectCode)).orElse(false))
                    .collect(Collectors.toSet());

            if (filteredCoreTransactions.isEmpty()) {
                log.warn("No core transactions to process for organisationId: {}", organisationId);
                return;
            }

            log.info("Publishing ERPIngestionEvent event, organisationId: {}, tx count: {}", organisationId, coreTransactions.size());

            val netsuiteIngestionId = netsuiteIngestion.getId();

            Iterables.partition(filteredCoreTransactions, sendBatchSize).forEach(txPartition -> {
                applicationEventPublisher.publishEvent(new ERPIngestionEvent(
                        netsuiteIngestionId,
                        initiator,
                        filteringParameters,
                        new OrganisationTransactions(organisationId, Sets.newHashSet(txPartition))));
            });
        });

        log.info("NetSuite ingestion completed.");
    }

    private Either<Problem, NetSuiteIngestion> retrieveAndStoreNetSuiteIngestion() {
        log.info("Running ingestion...");

        val netSuiteJsonE = retrieveLatestNetsuiteTransactionLines();

        if (netSuiteJsonE.isEmpty()) {
            log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."Error retrieving data from NetSuite API, url: \{netSuiteAPI.netsuiteUrl()}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));

            return Either.left(netSuiteJsonE.getLeft());
        }

        val bodyM = netSuiteJsonE.get();
        if (bodyM.isEmpty()) {
            log.warn("No data to read from NetSuite API..., bailing out!");

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."No data to read from NetSuite API, url: \{netSuiteAPI.netsuiteUrl()}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));

            return Either.left(issue);
        }

        val netsuiteTransactionLinesJson = bodyM.get();
        val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLinesJson);
        val netSuiteIngestion = new NetSuiteIngestion();

        val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);

        log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

        //netSuiteIngestion.setIngestionBody(compressedBody);
        netSuiteIngestion.setIngestionBody(netsuiteTransactionLinesJson);
        netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

        return Either.right(ingestionRepository.saveAndFlush(netSuiteIngestion));
    }

    private Either<Problem, List<SearchResultTransactionItem>> parseNetSuiteSearchResults(String jsonString) {
        try {
            val transactionDataSearchResult = objectMapper.readValue(jsonString, TransactionDataSearchResult.class);

            // TODO how to handle pagination and more results to fetch?
            if (transactionDataSearchResult.more()) {
                log.warn("More data available in the search result, pagination not implemented yet!");

                return Either.left(Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::PAGINATION_NOT_IMPLEMENTED")
                        .withDetail("More data available in the search result, pagination not implemented yet!")
                        .build()
                );
            }

            return Either.right(transactionDataSearchResult.lines());
        } catch (JsonProcessingException e) {
            log.error("Error parsing NetSuite search result: {}", e.getMessage(), e);

            return Either.left(Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::JSON_PARSE_ERROR")
                    .withDetail(STR."JSON rrror parsing NetSuite search error: \{e.getMessage()}")
                    .build());
        }
    }

}
