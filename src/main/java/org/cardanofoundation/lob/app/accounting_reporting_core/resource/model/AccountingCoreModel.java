package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionItemView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.TransactionView;
import org.cardanofoundation.lob.app.accounting_reporting_core.resource.views.ViolationView;

import java.util.Set;
import java.util.stream.Collectors;

public class AccountingCoreModel {
    public static Set<TransactionView> getTransaction(TransactionBatchEntity transactionBatchEntity) {
        return transactionBatchEntity.getTransactions().stream().map(AccountingCoreModel::getTransactionView).collect(Collectors.toSet());
    }

    public static TransactionView getTransactionView(TransactionEntity transactionEntity) {

        return new TransactionView(
                transactionEntity.getId(),
                transactionEntity.getTransactionInternalNumber(),
                transactionEntity.getEntryDate(),
                transactionEntity.getTransactionType(),
                transactionEntity.getValidationStatus(),
                transactionEntity.getTransactionApproved(),
                transactionEntity.getLedgerDispatchApproved(),
                getTransactionItemView(transactionEntity),
                getViolation(transactionEntity)
        );
    }

    public static Set<TransactionItemView> getTransactionItemView(TransactionEntity transaction) {
        return transaction.getItems().stream().map(item -> new TransactionItemView(
                item.getId(),
                item.getAccountDebit(),
                item.getAccountCredit(),
                item.getAmountFcy(),
                item.getAmountLcy()
        )).collect(Collectors.toSet());
    }

    public static Set<ViolationView> getViolation(TransactionEntity transaction) {

        return transaction.getViolations().stream().map(violation -> new ViolationView(
                violation.getType(),
                violation.getSource(),
                violation.getTxItemId(),
                violation.getCode(),
                violation.getBag()
        )).collect(Collectors.toSet());
    }
}
