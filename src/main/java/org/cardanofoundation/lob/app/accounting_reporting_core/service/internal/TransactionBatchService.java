package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepositoryGateway;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionBatchService {

    private final TransactionBatchRepositoryGateway transactionBatchRepositoryGateway;
    private final TransactionBatchRepository transactionBatchRepository;
    private final TransactionConverter transactionConverter;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createTransactionBatch(String batchId,
                                       String instanceId,
                                       String initiator,
                                       FilteringParameters filteringParameters) {
        log.info("Creating transaction batch, batchId: {}, initiator: {}, instanceId: {}, filteringParameters: {}", batchId, initiator, instanceId, filteringParameters);

        val transactionBatchEntity = new TransactionBatchEntity()
                .id(batchId)
                .transactions(Set.of()) // initially empty
                .filteringParameters(transactionConverter.convert(filteringParameters))
                .updatedBy(initiator)
                .createdBy(initiator);

        transactionBatchRepository.save((TransactionBatchEntity) transactionBatchEntity);

        log.info("Transaction batch created, batchId: {}", batchId);

        applicationEventPublisher.publishEvent(TransactionBatchCreatedEvent.builder()
                .batchId(batchId)
                .instanceId(instanceId)
                .filteringParameters(filteringParameters)
                .build());
    }

    @Transactional
    public void updateTransactionBatch(String organisationId,
                                       String batchId,
                                       Optional<Integer> totalTransactionsCount) {
        val txBatchM = transactionBatchRepositoryGateway.findById(batchId);

        if (txBatchM.isEmpty()) {
            log.warn("Transaction batch not found for id: {}", batchId);
            return;
        }

        val txBatch = txBatchM.orElseThrow();

        txBatch.batchStatistics(BatchStatistics.builder()
                .totalTransactionsCount(calcTotalTransactionsCount(txBatch.getBatchStatistics().flatMap(BatchStatistics::getTotalTransactionsCount), totalTransactionsCount).orElse(null))
                .processedTransactionsCount(txBatch.transactions().size())
                .approvedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(TransactionEntity::transactionApproved).count()).intValue())
                .approvedTransactionsDispatchCount(Long.valueOf(txBatch.transactions().stream().filter(TransactionEntity::ledgerDispatchApproved).count()).intValue())
                .failedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.validationStatus() == FAILED).count()).intValue())
                .dispatchedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.DISPATCHED).count()).intValue())
                .completedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.COMPLETED).count()).intValue())
                .finalizedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.FINALIZED).count()).intValue())
                .build());

        transactionBatchRepository.save(txBatch);
    }

    private Optional<Integer> calcTotalTransactionsCount(Optional<Integer> totalTransactionsCountM1,
                                                         Optional<Integer> totalTransactionsCountM2) {

        if (totalTransactionsCountM1.isEmpty() && totalTransactionsCountM2.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Math.max(totalTransactionsCountM1.orElse(-1), totalTransactionsCountM2.orElse(-1)));
    }

}
