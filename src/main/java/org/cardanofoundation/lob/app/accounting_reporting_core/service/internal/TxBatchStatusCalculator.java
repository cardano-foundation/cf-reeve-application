package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class TxBatchStatusCalculator {

    public TransactionBatchStatus reCalcStatus(TransactionBatchEntity transactionBatchEntity,
                                               Optional<Integer> totalTransactionsCount) {
        if (totalTransactionsCount.isPresent() && totalTransactionsCount.orElseThrow() == 0) {
            return TransactionBatchStatus.FINALIZED;
        }

        val allBatchTransactions = transactionBatchEntity.transactions();

        val validTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.validationStatus() == ValidationStatus.VALIDATED)
                .count();

        val dispatchedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.ledgerDispatchStatus() == LedgerDispatchStatus.DISPATCHED)
                .count();

        val completedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.ledgerDispatchStatus() == LedgerDispatchStatus.COMPLETED)
                .count();

        val finalisedTransactionsCount = allBatchTransactions
                .stream()
                .filter(transactionEntity -> transactionEntity.ledgerDispatchStatus() == LedgerDispatchStatus.FINALIZED)
                .count();

        if (dispatchedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.FINISHED;
        }
        if (completedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.COMPLETE;
        }
        if (finalisedTransactionsCount == validTransactionsCount) {
            return TransactionBatchStatus.FINALIZED;
        }

        return TransactionBatchStatus.PROCESSING;
    }

}
