package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.util.HashSet;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_CREDIT_IS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountCodeCreditCheckTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;

    @Override
    public TransactionWithViolations run(TransactionWithViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        if (tx.getTransactionType() == Journal) {
            return withPossibleViolations;
        }

        val violations = new HashSet<Violation>();
        for (val txItem : tx.getItems()) {
            if (txItem.getAccountCodeCredit().isEmpty())  {
                val v = Violation.create(
                        ERROR,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        ACCOUNT_CODE_CREDIT_IS_EMPTY,
                        pipelineTask.getClass().getSimpleName(),
                        Map.of("transactionNumber", tx.getInternalTransactionNumber())
                );

                violations.add(v);
            }
        }

        if (!violations.isEmpty()) {
            return TransactionWithViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

}
