package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;

import java.util.stream.Collectors;

@Slf4j
public class DebitAccountCheckTaskItem implements PipelineTaskItem {

    @Override
    public TransactionWithViolations run(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        // we accept only transaction items that are NOT sending to the same account, if they are we discard them
        val newItems = tx.getItems()
                .stream()
                .filter(txItem -> !txItem.getAccountCodeDebit().equals(txItem.getAccountCodeCredit()))
                .collect(Collectors.toSet());

        return TransactionWithViolations.create(tx
                .toBuilder()
                .items(newItems)
                .build()
        );
    }

}
