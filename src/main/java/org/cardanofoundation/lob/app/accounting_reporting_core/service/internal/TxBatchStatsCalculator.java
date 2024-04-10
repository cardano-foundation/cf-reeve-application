package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Optional;

@Service
@Slf4j
public class TxBatchStatsCalculator {

    public BatchStatistics reCalcStats(TransactionBatchEntity txBatch,
                                       Optional<Integer> totalTransactionsCount) {
        return BatchStatistics.builder()
                .totalTransactionsCount(totalTransactionsCount.orElse(null))
                .processedTransactionsCount(txBatch.transactions().size())
                .approvedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(TransactionEntity::transactionApproved).count()).intValue())
                .approvedTransactionsDispatchCount(Long.valueOf(txBatch.transactions().stream().filter(TransactionEntity::ledgerDispatchApproved).count()).intValue())
                .failedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.validationStatus() == ValidationStatus.FAILED).count()).intValue())
                .dispatchedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.DISPATCHED).count()).intValue())
                .completedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.COMPLETED).count()).intValue())
                .finalizedTransactionsCount(Long.valueOf(txBatch.transactions().stream().filter(tx -> tx.ledgerDispatchStatus() == LedgerDispatchStatus.FINALIZED).count()).intValue())
                .build();
    }

}
