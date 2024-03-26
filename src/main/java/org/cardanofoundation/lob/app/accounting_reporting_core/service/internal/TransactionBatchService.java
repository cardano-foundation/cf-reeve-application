package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus.CREATED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus.FINALIZED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionBatchService {

    private final TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;
    private final TransactionBatchRepository transactionBatchRepository;
    private final TransactionConverter transactionConverter;
    private final TransactionBatchAssocRepository transactionBatchAssocRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TxBatchStatusCalculator txBatchStatusCalculator;
    private final TxBatchStatsCalculator txBatchStatsCalculator;

    @Transactional
    public void createTransactionBatch(String batchId,
                                       String instanceId,
                                       String initiator,
                                       UserExtractionParameters userExtractionParameters,
                                       SystemExtractionParameters systemExtractionParameters) {
        log.info("Creating transaction batch, batchId: {}, initiator: {}, instanceId: {}, filteringParameters: {}", batchId, initiator, instanceId, userExtractionParameters);

        val transactionBatchEntity = new TransactionBatchEntity()
                .id(batchId)
                .transactions(Set.of()) // initially empty
                .filteringParameters(transactionConverter.convert(systemExtractionParameters, userExtractionParameters))
                .status(CREATED)
                .updatedBy(initiator)
                .createdBy(initiator);

        transactionBatchRepository.save((TransactionBatchEntity) transactionBatchEntity);

        log.info("Transaction batch created, batchId: {}", batchId);

        applicationEventPublisher.publishEvent(TransactionBatchCreatedEvent.builder()
                .batchId(batchId)
                .instanceId(instanceId)
                .userExtractionParameters(userExtractionParameters)
                .systemExtractionParameters(systemExtractionParameters)
                .build());
    }

    @Transactional
    public void updateTransactionBatchStatusAndStats(String batchId,
                                                     Optional<Integer> totalTransactionsCount) {
        val txBatchM = transactionBatchRepositoryGateway.findById(batchId);

        if (txBatchM.isEmpty()) {
            log.warn("Transaction batch not found for id: {}", batchId);
            return;
        }

        val txBatch = txBatchM.orElseThrow();

        if (txBatch.status() == FINALIZED) {
            log.info("Transaction batch already finalized or failed, batchId: {}", batchId);
            return;
        }

        val totalTxCount = totalTxCount(txBatch, totalTransactionsCount);

        txBatch.batchStatistics(txBatchStatsCalculator.reCalcStats(txBatch, totalTxCount));
        txBatch.status(txBatchStatusCalculator.reCalcStatus(txBatch, totalTxCount));

        transactionBatchRepository.save(txBatch);
    }

    private static Optional<Integer> totalTxCount(TransactionBatchEntity txBatch,
                                                  Optional<Integer> totalTransactionsCount) {
        return Optional.ofNullable(totalTransactionsCount
                .orElse(txBatch.getBatchStatistics().
                        flatMap(BatchStatistics::getTotalTransactionsCount)
                        .orElse(null)));
    }

    @Transactional
    public void updateBatchesPerTransactions(Set<TxStatusUpdate> txStatusUpdates) {
        for (val txStatusUpdate : txStatusUpdates) {
            val txId = txStatusUpdate.getTxId();
            val transactionBatchAssocs = transactionBatchAssocRepository.findAllByTxId(txId);

            if (transactionBatchAssocs.isEmpty()) {
                log.warn("Transaction batch assoc not found for id: {}", txId);
                continue;
            }

            val allBatchesIdsAssociatedWithThisTransaction = transactionBatchAssocs.stream()
                    .map(id -> id.id().transactionBatchId())
                    .collect(Collectors.toSet());

            transactionBatchRepository.findAllById(allBatchesIdsAssociatedWithThisTransaction)
                    .forEach(txBatch -> updateTransactionBatchStatusAndStats(txBatch.id(), Optional.empty()));
        }
    }

    @Transactional
    public List<TransactionBatchEntity> findAll() {
        return transactionBatchRepository.findAll();
    }

    @Transactional
    public Optional<TransactionBatchEntity> findById(String batchId) {
        return transactionBatchRepository.findById(batchId);
    }

}
