package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity.NetSuiteIngestionEntity;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreCompress;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FatalError.ErrorCode.INTERNAL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent.Status.*;
import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.util.MoreCompress.decompress;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;
import static org.cardanofoundation.lob.app.support.crypto.MD5Hashing.md5;
import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final NetSuiteClient netSuiteClient;

    private final TransactionConverter transactionConverter;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SystemExtractionParametersFactory systemExtractionParametersFactory;

    private final ExtractionParametersFilteringService extractionParametersFilteringService;

    private final NetSuiteParser netSuiteParser;

    private final int sendBatchSize;

    private final String netsuiteInstanceId;

    //@Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}")
    private final boolean isNetSuiteInstanceDebugMode;

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestionEntity> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

    @Transactional
    public void startNewERPExtraction(String organisationId,
                                      String initiator,
                                      UserExtractionParameters userExtractionParameters) {
        val batchId = digestAsHex(UUID.randomUUID().toString());

        try {
            log.info("Running ingestion...");

            val netSuiteJsonE = netSuiteClient.retrieveLatestNetsuiteTransactionLines();

            if (netSuiteJsonE.isEmpty()) {
                log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                        .withDetail(STR."Error retrieving data from NetSuite API, url: \{netSuiteClient.netsuiteUrl()}")
                        .build();

                log.error(STR."NetSuite Adapter, issue: \{issue}");

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

                log.error(STR."NetSuite Adapter, issue: \{issue}");

                applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
                return;
            }

            val netsuiteTransactionLinesJson = bodyM.get();
            val ingestionBodyChecksum = md5(netsuiteTransactionLinesJson);
            val netSuiteIngestion = new NetSuiteIngestionEntity();
            netSuiteIngestion.setId(batchId);

            val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);
            if (compressedBody == null) {
                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::NETSUITE_API_ERROR")
                        .withDetail(STR."Error compressing data from NetSuite API, url: \{netSuiteClient.netsuiteUrl()}")
                        .build();

                log.error(STR."NetSuite Adapter, issue: \{issue}");

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

            val systemExtractionParametersE = systemExtractionParametersFactory.createSystemExtractionParameters(organisationId);
            if (systemExtractionParametersE.isLeft()) {
                log.error("Error creating system extraction parameters: {}", systemExtractionParametersE.getLeft().getDetail());

                val issue = NotificationEvent.create(ERROR, systemExtractionParametersE.getLeft());

                log.error(STR."NetSuite Adapter, issue: \{issue}");

                applicationEventPublisher.publishEvent(issue);
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
        } catch (Exception e) {
            val bag= Map.<String, Object>of(
                    "adapterInstanceId", netsuiteInstanceId,
                    "technicalErrorMessage", e.getMessage()
            );

            applicationEventPublisher.publishEvent(ERPIngestionStored.builder()
                    .batchId(batchId)
                    .organisationId(userExtractionParameters.getOrganisationId())
                    .instanceId(netsuiteInstanceId)
                    .initiator(initiator)
                    .userExtractionParameters(userExtractionParameters)
                    .fatalError(Optional.of(new FatalError(INTERNAL, bag)))
                    .systemExtractionParameters(SystemExtractionParameters.builder()
                            .organisationId(organisationId)
                            .accountPeriodFrom(YearMonth.now()) // fake params
                            .accountPeriodTo(YearMonth.now()) // fake params
                            .build())
                    .build());
        }
    }

    @Transactional
    public void continueERPExtraction(String batchId,
                                      String organisationId,
                                      String instanceId,
                                      UserExtractionParameters userExtractionParameters,
                                      SystemExtractionParameters systemExtractionParameters
    ) {
        try {
            log.info("Continuing ERP extraction..., batchId: {}, instanceId: {}", batchId, instanceId);

            val netsuiteIngestionM = ingestionRepository.findById(batchId);
            if (netsuiteIngestionM.isEmpty()) {
                log.error("NetSuite ingestion not found, batchId: {}", batchId);

                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::NETSUITE_INGESTION_NOT_FOUND")
                        .withDetail(STR."NetSuite ingestion not found, batchId: \{batchId}")
                        .build();

                log.error(STR."NetSuite Adapter, issue: \{issue}");

                applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
                return;
            }

            if (!userExtractionParameters.getOrganisationId().equals(systemExtractionParameters.getOrganisationId())) {
                val issue = Problem.builder()
                        .withTitle("NETSUITE_ADAPTER::ORGANISATION_MISMATCH")
                        .withDetail(STR."Organisation mismatch, userExtractionParameters.organisationId: \{userExtractionParameters.getOrganisationId() }, systemExtractionParameters.organisationId: \{systemExtractionParameters.getOrganisationId()}")
                        .build();

                log.error(STR."NetSuite Adapter, issue: \{issue}");

                applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, issue));
                return;
            }
            val netsuiteIngestion = netsuiteIngestionM.orElseThrow();

            val transactionDataSearchResultE = netSuiteParser.parseSearchResults(requireNonNull(decompress(netsuiteIngestion.getIngestionBody())));

            if (transactionDataSearchResultE.isEmpty()) {
                log.warn("Error parsing NetSuite search result: {}", transactionDataSearchResultE.getLeft());

                val issue = NotificationEvent.create(ERROR, transactionDataSearchResultE.getLeft());

                log.error(STR."NetSuite Adapter, issue: \{issue}");

                applicationEventPublisher.publishEvent(issue);
                return;
            }

            val transactionDataSearchResult = transactionDataSearchResultE.get();
            val transactionsE = transactionConverter.convert(organisationId, batchId, transactionDataSearchResult);

            if (transactionsE.isLeft()) {
                val batchChunkEventBuilder = TransactionBatchChunkEvent.builder()
                        .batchId(netsuiteIngestion.getId())
                        .organisationId(organisationId)
                        .systemExtractionParameters(systemExtractionParameters)
                        .totalTransactionsCount(Optional.of(0))
                        .status(FINISHED)
                        .fatalError(Optional.of(transactionsE.getLeft()))
                        .transactions(Set.of());

                applicationEventPublisher.publishEvent(batchChunkEventBuilder.build());

                return;
            }

            val transactions = transactionsE.get();

            val transactionsWithExtractionParametersApplied = extractionParametersFilteringService
                    .applyExtractionParameters(userExtractionParameters, systemExtractionParameters, transactions.transactions());

            Partitions.partition(transactionsWithExtractionParametersApplied, sendBatchSize).forEach(txPartition -> {
                val batchChunkEventBuilder = TransactionBatchChunkEvent.builder()
                        .batchId(netsuiteIngestion.getId())
                        .organisationId(organisationId)
                        .systemExtractionParameters(systemExtractionParameters)
                        .totalTransactionsCount(Optional.of(transactionsWithExtractionParametersApplied.size()))
                        .transactions(txPartition.asSet());
                if (txPartition.isFirst()) {
                    batchChunkEventBuilder.status(STARTED);
                } else if (txPartition.isLast()) {
                    batchChunkEventBuilder.status(FINISHED);
                } else {
                    batchChunkEventBuilder.status(PROCESSING);
                }

                applicationEventPublisher.publishEvent(batchChunkEventBuilder.build());
            });

            log.info("NetSuite ingestion fully completed.");
        } catch (Exception e) {
            log.error("Fatal error while processing NetSuite ingestion", e);

            val bag= Map.<String, Object>of(
                    "adapterInstanceId", netsuiteInstanceId,
                    "technicalErrorMessage", e.getMessage()
            );

            val batchChunkEventBuilder = TransactionBatchChunkEvent.builder()
                    .batchId(batchId)
                    .organisationId(organisationId)
                    .systemExtractionParameters(systemExtractionParameters)
                    .totalTransactionsCount(Optional.of(0))
                    .status(FINISHED)
                    .fatalError(Optional.of(new FatalError(INTERNAL, bag)))
                    .transactions(Set.of());

            applicationEventPublisher.publishEvent(batchChunkEventBuilder.build());
        }
    }

}
