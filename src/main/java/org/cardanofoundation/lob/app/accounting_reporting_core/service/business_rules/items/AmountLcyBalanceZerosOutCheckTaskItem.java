package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;


import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.LCY_BALANCE_MUST_BE_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AmountLcyBalanceZerosOutCheckTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val txItems = tx.getItems();
        val lcySum = txItems.stream().map(TransactionItem::getAmountLcy).reduce(ZERO, BigDecimal::add);

        if (lcySum.signum() != 0) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    LCY_BALANCE_MUST_BE_ZERO,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        return violationTransaction;
    }

}
