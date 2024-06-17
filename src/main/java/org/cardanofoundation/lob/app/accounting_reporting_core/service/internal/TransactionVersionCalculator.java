package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionVersionAlgo;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.cardanofoundation.lob.app.support.crypto.SHA3;

@Slf4j
public class TransactionVersionCalculator {

    public static String compute(TransactionVersionAlgo algo, TransactionEntity transactionEntity) {
        val b = new StringBuilder();

        b.append(transactionEntity.getId());
        b.append(transactionEntity.getTransactionInternalNumber());
        b.append(compute(algo, transactionEntity.getOrganisation()));
        b.append(STR."\{transactionEntity.getAccountingPeriod().getMonth().getValue()}-\{transactionEntity.getAccountingPeriod().getYear()}");
        b.append(transactionEntity.getTransactionType());
        b.append(transactionEntity.getEntryDate());

        for (val item : transactionEntity.getItems()) {
            b.append(compute(algo, item));
        }

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, TransactionItemEntity item) {
        val b = new StringBuilder();

        b.append(item.getId());

        item.getAccountCredit().ifPresent(acc -> b.append(compute(algo, acc)));
        item.getAccountDebit().ifPresent(acc -> b.append(compute(algo, acc)));

        b.append(item.getAmountFcy());
        b.append(item.getAmountLcy());

        b.append(item.getFxRate());

        item.getCostCenter().ifPresent(cc -> b.append(compute(algo, cc)));
        item.getDocument().ifPresent(d -> b.append(compute(algo, d)));
        item.getProject().ifPresent(p -> b.append(compute(algo, p)));

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Document document) {
        val b = new StringBuilder();

        b.append(document.getNum());
        document.getCounterparty().ifPresent(cp -> b.append(compute(algo, cp)));
        document.getVat().ifPresent(v -> b.append(compute(algo, v)));
        b.append(document.getCurrency().getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Vat vat) {
        val b = new StringBuilder();

        b.append(vat.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Counterparty counterparty) {
        val b = new StringBuilder();

        b.append(counterparty.getCustomerCode());
        b.append(counterparty.getType());
        counterparty.getName().ifPresent(b::append);

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, CostCenter costCenter) {
        val b = new StringBuilder();

        b.append(costCenter.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Project project) {
        val b = new StringBuilder();

        b.append(project.getCustomerCode());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Organisation org) {
        val b = new StringBuilder();

        b.append(org.getId());

        return SHA3.digestAsHex(b.toString());
    }

    private static String compute(TransactionVersionAlgo algo, Account acc) {
        val b = new StringBuilder();

        b.append(acc.getCode());
        b.append(acc.getName());

        return SHA3.digestAsHex(b.toString());
    }

}
