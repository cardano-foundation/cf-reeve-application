package org.cardanofoundation.lob.app.netsuite_adapter.service;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestionEntity;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.Optional;
import java.util.UUID;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent.Status.*;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress.compress;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress.decompress;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;
import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final NetSuiteClient netSuiteClient;

    private final NotificationsSenderService notificationsSenderService;

    private final ViolationsSenderService violationsSenderService;

    private final TransactionConverter transactionConverter;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SystemExtractionParametersFactory systemExtractionParametersFactory;

    private final ExtractionParametersFilteringService extractionParametersFilteringService;

    private final NetSuiteParser netSuiteParser;

    @Value("${lob.events.netsuite.to.core.send.batch.size:100}")
    private int sendBatchSize = 100;

    @Value("${lob.events.netsuite.to.core.netsuite.instance.id:fEU237r9rqAPEGEFY1yr}")
    private String netsuiteInstanceId;

    @Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}")
    private boolean isNetSuiteInstanceDebugMode;

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestionEntity> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines() {
        return netSuiteClient.retrieveLatestNetsuiteTransactionLines();
    }

    @Transactional
    public void startERPExtraction(String initiator,
                                   UserExtractionParameters userExtractionParameters) {
        log.info("Running ingestion...");

        val netSuiteJsonE = retrieveLatestNetsuiteTransactionLines();

        if (netSuiteJsonE.isEmpty()) {
            log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."Error retrieving data from NetSuite API, url: \{netSuiteClient.netsuiteUrl()}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
            return;
        }

        val bodyM = netSuiteJsonE.get();
        if (bodyM.isEmpty()) {
            log.warn("No data to read from NetSuite API..., bailing out!");

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."No data to read from NetSuite API, url: \{netSuiteClient.netsuiteUrl()}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
            return;
        }

        val netsuiteTransactionLinesJson = bodyM.get();
        val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLinesJson);
        val netSuiteIngestion = new NetSuiteIngestionEntity();
        netSuiteIngestion.setId(digestAsHex(UUID.randomUUID().toString()));

        val compressedBody = compress(netsuiteTransactionLinesJson);
        if (compressedBody == null) {
            log.error("Error compressing data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                    .withDetail(STR."Error compressing data from NetSuite API, url: \{netSuiteClient.netsuiteUrl()}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
            return;
        }

        log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

        netSuiteIngestion.setIngestionBody(compressedBody);
        if (isNetSuiteInstanceDebugMode) {
            netSuiteIngestion.setIngestionBodyDebug(netsuiteTransactionLinesJson);
        }
        netSuiteIngestion.setInstanceId(netsuiteInstanceId);
        netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

        val storedNetsuiteIngestion = ingestionRepository.saveAndFlush(netSuiteIngestion);

        val systemExtractionParametersE = systemExtractionParametersFactory.createSystemExtractionParameters(userExtractionParameters.getOrganisationId());
        if (systemExtractionParametersE.isLeft()) {
            log.error("Error creating system extraction parameters: {}", systemExtractionParametersE.getLeft().getDetail());

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, systemExtractionParametersE.getLeft()));
            return;
        }

        applicationEventPublisher.publishEvent(ERPIngestionStored.builder()
                .batchId(storedNetsuiteIngestion.getId())
                .organisationId(userExtractionParameters.getOrganisationId())
                .instanceId(netsuiteInstanceId)
                .initiator(initiator)
                .userExtractionParameters(userExtractionParameters)
                .systemExtractionParameters(systemExtractionParametersE.get())
                .build()
        );

        log.info("NetSuite ingestion completed.");
    }

    @Transactional
    public void continueERPExtraction(String batchId,
                                      String instanceId,
                                      UserExtractionParameters userExtractionParameters,
                                      SystemExtractionParameters systemExtractionParameters
    ) {
        log.info("Continuing ERP extraction..., batchId: {}, instanceId: {}", batchId, instanceId);

        val netsuiteIngestionM = ingestionRepository.findById(batchId);
        if (netsuiteIngestionM.isEmpty()) {
            log.error("NetSuite ingestion not found, batchId: {}", batchId);

            val issue = Problem.builder()
                    .withTitle("NETSUITE_ADAPTER::NETSUITE_INGESTION_NOT_FOUND")
                    .withDetail(STR."NetSuite ingestion not found, batchId: \{batchId}")
                    .build();

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
            return;
        }

        val netsuiteIngestion = netsuiteIngestionM.orElseThrow();

        val transactionDataSearchResultE = netSuiteParser.parseSearchResults(decompress(netsuiteIngestion.getIngestionBody()));

        if (transactionDataSearchResultE.isEmpty()) {
            log.warn("Error parsing NetSuite search result: {}", transactionDataSearchResultE.getLeft());

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, transactionDataSearchResultE.getLeft()));
            return;
        }

        val transactionDataSearchResult = transactionDataSearchResultE.get();

        val transactionsWithViolations = transactionConverter.convert(transactionDataSearchResult, batchId);

        violationsSenderService.sendViolation(transactionsWithViolations);
        notificationsSenderService.sendNotifications(transactionsWithViolations.violations());

        val coreTransactionsToOrganisationMap = transactionsWithViolations.transactionPerOrganisationId();

        log.info("coreTransactionLines count: {}", coreTransactionsToOrganisationMap.size());

        coreTransactionsToOrganisationMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(systemExtractionParameters.getOrganisationId())
                        && entry.getKey().equals(userExtractionParameters.getOrganisationId()))
                .forEach(entry -> {
                    val organisationId = entry.getKey();
                    val transactions = entry.getValue();

                    val transactionsWithExtractionParametersApplied = extractionParametersFilteringService
                            .applyExtractionParameters(userExtractionParameters, systemExtractionParameters, transactions);

                    log.info("after filtering tx count: {}", transactionsWithExtractionParametersApplied.size());

                    if (transactionsWithExtractionParametersApplied.isEmpty()) {
                        log.warn("No core passedTransactions to process for organisationId: {}", organisationId);

                        // TODO send batch failed event?
                        return;
                    }

                    log.info("Publishing ERPIngestionEvent event, organisationId: {}, tx count: {}", organisationId, transactions.size());

                    Partitions.partition(transactionsWithExtractionParametersApplied, sendBatchSize).forEach(txPartition -> {
                        val eventBuilder = TransactionBatchChunkEvent.builder()
                                .batchId(netsuiteIngestion.getId())
                                .organisationId(organisationId)
                                .systemExtractionParameters(systemExtractionParameters)
                                .totalTransactionsCount(Optional.of(transactionsWithExtractionParametersApplied.size()))
                                .transactions(txPartition.asSet());

                        if (txPartition.isFirst()) {
                            eventBuilder.status(STARTED);
                        } else if (txPartition.isLast()) {
                            eventBuilder.status(FINISHED);
                        } else {
                            eventBuilder.status(PROCESSING);
                        }

                        applicationEventPublisher.publishEvent(eventBuilder.build());
                    });
                });

        log.info("NetSuite ingestion fully completed.");
    }

}
