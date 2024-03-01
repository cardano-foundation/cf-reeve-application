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
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NetSuiteAPI netSuiteAPI;

    private final ObjectMapper objectMapper;

    private final TransactionConverter transactionConverter;

    private final BusinessRulesPipelineProcessor businessRulesPipelineProcessor;

    // TODO this this over properly
    @Value("${lob.connector.id:jhu765}")
    private String connectorId;

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

            log.warn("Error converting NetSuite search, issue:{}", issue);

            return;
        }
        val coreTransactionsToOrganisationMap = coreTransactionsE.get();

        log.info("CoreTransactionLines count: {}", coreTransactionsToOrganisationMap.size());

        val lotId = netsuiteIngestion.getId();

        coreTransactionsToOrganisationMap.forEach((organisationId, coreTransactions) -> {
            log.info("before business process rules tx count: {}", coreTransactions.size());

            val transformationResult = businessRulesPipelineProcessor.run(
                    new OrganisationTransactions(organisationId, coreTransactions),
                    OrganisationTransactions.empty(organisationId),
                    new HashSet<>());

            val txs = transformationResult.organisationTransactions().transactions();
            log.info("after business process tx count: {}", txs.size());

            val transactionsWithExtractionParametersApplied = applyExtractionParameters(filteringParameters, txs);

            log.info("after filtering tx count: {}", transactionsWithExtractionParametersApplied.size());

            if (transactionsWithExtractionParametersApplied.isEmpty()) {
                log.warn("No core organisationTransactions to process for organisationId: {}", organisationId);
                return;
            }

            log.info("Publishing ERPIngestionEvent event, organisationId: {}, tx count: {}", organisationId, coreTransactions.size());

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
                                                              Set<Transaction> coreTransactions) {
        return coreTransactions.stream()
                .filter(line -> {
                    val fromM = filteringParameters.getFrom();

                    return fromM.isEmpty()
                            || fromM.map(date -> line.getEntryDate().isEqual(date)).orElse(true)
                            || fromM.map(date -> line.getEntryDate().isAfter(date)).orElse(true);
                })
                .filter(line -> {
                    val toM = filteringParameters.getTo();

                    return toM.isEmpty()
                            || toM.map(date -> line.getEntryDate().isEqual(date)).orElse(true)
                            || toM.map(date -> line.getEntryDate().isBefore(date)).orElse(true);
                })
                .filter(line -> {
                    val txTypes = filteringParameters.getTransactionTypes();

                    return txTypes.isEmpty() || txTypes.contains(line.getTransactionType());
                })
                .filter(line -> {
                    val organisationIds = filteringParameters.getOrganisationIds();

                    return organisationIds.isEmpty() || organisationIds.contains(line.getOrganisation().getId());
                })
                .filter(line -> {
                    val projectCodes = filteringParameters.getProjectCodes();

                    return projectCodes.isEmpty() || line.getProject().flatMap(Project::getCode).map(projectCodes::contains).orElse(true);
                })
                .filter(line -> {
                    val costCenterNames = filteringParameters.getCostCenterCodes();

                    return costCenterNames.isEmpty() || line.getCostCenter().flatMap(CostCenter::getCode).map(costCenterNames::contains).orElse(true);
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
