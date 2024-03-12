package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import io.vavr.Predicates;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;

import java.util.stream.Collectors;

public class DiscardZeroBalanceTxItemsTaskItem implements PipelineTaskItem {

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val txItems = tx.getItems()
                .stream()
                .filter(Predicates.not(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0))
                .collect(Collectors.toSet());

        return TransactionWithViolations.create(
                tx.toBuilder()
                        .items(txItems)
                        .build()
        );
    }

}
