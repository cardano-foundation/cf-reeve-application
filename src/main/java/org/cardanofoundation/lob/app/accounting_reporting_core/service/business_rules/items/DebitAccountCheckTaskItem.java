package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class DebitAccountCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getValidationStatus() == FAILED) {
            return;
        }

        tx.getItems().removeIf(txItem -> txItem.getAccountCodeDebit().equals(txItem.getAccountCodeCredit()));
    }

}
