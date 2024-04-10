package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import io.vavr.Predicates;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;

import java.util.stream.Collectors;

public class DiscardZeroBalanceTxItemsTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        val txItems = tx.getItems()
                .stream()
                .filter(Predicates.not(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0))
                .collect(Collectors.toSet());

        return tx.toBuilder()
                .items(txItems)
                .build();
    }

}
