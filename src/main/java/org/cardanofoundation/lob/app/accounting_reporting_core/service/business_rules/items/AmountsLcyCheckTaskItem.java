package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;


import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.util.HashSet;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.AMOUNT_LCY_IS_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AmountsLcyCheckTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        for (val txItem : tx.getItems()) {
            if (txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() != 0) {
                val v = Violation.create(
                        ERROR,
                        ERP,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        AMOUNT_LCY_IS_ZERO,
                        pipelineTask.getClass().getSimpleName(),
                        Map.of(
                                "transactionNumber", tx.getInternalTransactionNumber(),
                                "amountFcy", txItem.getAmountFcy(),
                                "amountLcy", txItem.getAmountLcy()
                        )
                );

                violations.add(v);
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return TransactionWithViolations.create(tx.toBuilder().validationStatus(FAILED).build(), violations);
    }

}
