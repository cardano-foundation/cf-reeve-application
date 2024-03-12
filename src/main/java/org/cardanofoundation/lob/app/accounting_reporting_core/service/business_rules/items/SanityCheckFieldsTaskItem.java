package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.util.HashSet;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_SANITY_CHECK_FAIL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class SanityCheckFieldsTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final Validator validator;

    @Override
    public TransactionWithViolations run(TransactionWithViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();
        val violations = new HashSet<Violation>();

        val errors = validator.validate(tx);

        val notFailedYet = withPossibleViolations.violations()
                .stream()
                .noneMatch(v -> v.transactionId().equals(tx.getId()));

        if (!errors.isEmpty() && notFailedYet) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    TX_SANITY_CHECK_FAIL,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber(),
                            "errors", errors
                    )
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return TransactionWithViolations
                    .create(tx.toBuilder()
                                    .validationStatus(FAILED).build(),
                            violations
                    );
        }

        return withPossibleViolations;
    }

}
