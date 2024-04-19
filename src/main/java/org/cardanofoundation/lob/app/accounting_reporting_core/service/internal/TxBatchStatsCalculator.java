package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Service
@Slf4j
public class TxBatchStatsCalculator {

    public BatchStatistics reCalcStats(TransactionBatchEntity txBatch,
                                       Optional<Integer> totalTransactionsCount) {

        return BatchStatistics.builder()
                .totalTransactionsCount(totalTransactionsCount.orElse(null))
                .processedTransactionsCount(txBatch.getTransactions().size())
                .approvedTransactionsCount(Long.valueOf(txBatch.getTransactions().stream().filter(TransactionEntity::getTransactionApproved).count()).intValue())
                .approvedTransactionsDispatchCount(Long.valueOf(txBatch.getTransactions().stream().filter(TransactionEntity::getLedgerDispatchApproved).count()).intValue())
                .failedTransactionsCount(Long.valueOf(txBatch.getTransactions().stream().filter(tx -> tx.getValidationStatus() == FAILED).count()).intValue())
                .dispatchedTransactionsCount(Long.valueOf(txBatch.getTransactions().stream().filter(tx -> tx.getLedgerDispatchStatus() == DISPATCHED).count()).intValue())
                .completedTransactionsCount(Long.valueOf(txBatch.getTransactions().stream().filter(tx -> tx.getLedgerDispatchStatus() == COMPLETED).count()).intValue())
                .finalizedTransactionsCount(Long.valueOf(txBatch.getTransactions().stream().filter(tx -> tx.getLedgerDispatchStatus() == FINALIZED).count()).intValue())
                .build();
    }

}
