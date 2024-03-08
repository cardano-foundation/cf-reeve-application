package org.cardanofoundation.lob.app.netsuite_adapter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TxLine;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress.decompress;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NetSuiteAPI netSuiteAPI;

    private final ObjectMapper objectMapper;

    private final TransactionConverter transactionConverter;

    @Value("${lob.events.netsuite.to.core.send.batch.size:25}")
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
            log.error("Error retrieving data from NetSuite API: {}", netsuiteTransactionLinesJsonE.getLeft().getDetail());
            return;
        }

        val netsuiteIngestion = netsuiteTransactionLinesJsonE.get();
        val netsuiteTransactionLinesJson = decompress(netsuiteIngestion.getIngestionBody());

        val transactionDataSearchResultE = parseNetSuiteSearchResults(netsuiteTransactionLinesJson);

        if (transactionDataSearchResultE.isEmpty()) {
            log.warn("Error parsing NetSuite search result: {}", transactionDataSearchResultE.getLeft().getDetail());
            return;
        }

        val transactionDataSearchResult = transactionDataSearchResultE.get();

        val transactionsWithViolations = transactionConverter.convert(transactionDataSearchResult);

        if (!transactionsWithViolations.violations().isEmpty()) {
            val netsuiteViolations = transactionsWithViolations.violations();

            netsuiteViolations.forEach(n -> log.warn("NETSUITE VIOLATION: {}", n));

            val notifications = netsuiteViolations.stream()
                    .map(violation -> Problem.builder()
                            .withTitle("NETSUITE_ADAPTER::TRANSACTION_CONVERSION_ERROR")
                            .withDetail(STR."Error converting NetSuite response, violation: \{violation}")
                            .with("violation", violation)
                            .build())
                    .collect(Collectors.toSet());

            notifications.forEach(applicationEventPublisher::publishEvent);
        }

        val coreTransactionsToOrganisationMap = transactionsWithViolations.transactionPerOrganisationId();

        log.info("CoreTransactionLines count: {}", coreTransactionsToOrganisationMap.size());

        val lotId = netsuiteIngestion.getId();

        coreTransactionsToOrganisationMap.forEach((organisationId, transactions) -> {
            log.info("before business process rules tx count: {}", transactions.size());

            val transactionsWithExtractionParametersApplied = applyExtractionParameters(filteringParameters, transactions);

            log.info("after filtering tx count: {}", transactionsWithExtractionParametersApplied.size());

            if (transactionsWithExtractionParametersApplied.isEmpty()) {
                log.warn("No core organisationTransactions to process for organisationId: {}", organisationId);
                return;
            }

            log.info("Publishing ERPIngestionEvent event, organisationId: {}, tx count: {}", organisationId, transactions.size());

            Iterables.partition(transactionsWithExtractionParametersApplied, sendBatchSize).forEach(txPartition -> {
                applicationEventPublisher.publishEvent(new ERPIngestionEvent(
                        lotId,
                        initiator,
                        filteringParameters,
                        new OrganisationTransactions(organisationId, Sets.newHashSet(txPartition))));
            });

        });

        log.info("NetSuite ingestion completed.");
    }

    private static Set<Transaction> applyExtractionParameters(FilteringParameters filteringParameters,
                                                              Set<Transaction> txs) {
        return txs.stream()
                .filter(tx -> {
                    val fromM = filteringParameters.getFrom();

                    return fromM.isEmpty()
                            || fromM.map(date -> tx.getEntryDate().isEqual(date)).orElse(true)
                            || fromM.map(date -> tx.getEntryDate().isAfter(date)).orElse(true);
                })
                .filter(tx -> {
                    val toM = filteringParameters.getTo();

                    return toM.isEmpty()
                            || toM.map(date -> tx.getEntryDate().isEqual(date)).orElse(true)
                            || toM.map(date -> tx.getEntryDate().isBefore(date)).orElse(true);
                })
                .filter(tx -> {
                    val txTypes = filteringParameters.getTransactionTypes();

                    return txTypes.isEmpty() || txTypes.contains(tx.getTransactionType());
                })
                .filter(tx -> {
                    val organisationIds = filteringParameters.getOrganisationIds();

                    return organisationIds.isEmpty() || organisationIds.contains(tx.getOrganisation().getId());
                })
                .filter(tx -> {
                    val projectCodes = filteringParameters.getProjectCodes();

                    return projectCodes.isEmpty() || tx.getProject().map(Project::getCustomerCode).map(projectCodes::contains).orElse(true);
                })
                .filter(tx -> {
                    val costCenterCodes = filteringParameters.getCostCenterCodes();

                    return costCenterCodes.isEmpty() || tx.getCostCenter().map(CostCenter::getCustomerCode).map(costCenterCodes::contains).orElse(true);
                })
                .collect(Collectors.toSet());
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

            return Either.left(issue);
        }

        val bodyM = netSuiteJsonE.get();
        if (bodyM.isEmpty()) {
            log.warn("No data to read from NetSuite API..., bailing out!");

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."No data to read from NetSuite API, url: \{netSuiteAPI.netsuiteUrl()}")
                    .build();

            return Either.left(issue);
        }

        val netsuiteTransactionLinesJson = bodyM.get();
        val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLinesJson);
        val netSuiteIngestion = new NetSuiteIngestion();

        val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);

        log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

        netSuiteIngestion.setIngestionBody(compressedBody);
        //netSuiteIngestion.setIngestionBody(netsuiteTransactionLinesJson);
        netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

        return Either.right(ingestionRepository.saveAndFlush(netSuiteIngestion));
    }

    private Either<Problem, List<TxLine>> parseNetSuiteSearchResults(String jsonString) {
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
