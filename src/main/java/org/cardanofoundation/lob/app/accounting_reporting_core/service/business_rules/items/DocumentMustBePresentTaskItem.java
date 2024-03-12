package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.util.HashSet;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.DOCUMENT_MUST_BE_PRESENT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class DocumentMustBePresentTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;

    @Override
    public TransactionWithViolations run(TransactionWithViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getDocument().isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    DOCUMENT_MUST_BE_PRESENT,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return TransactionWithViolations
                    .create(tx.toBuilder()
                                    .validationStatus(FAILED)
                                    .build(),
                            violations);
        }

        return withPossibleViolations;
    }

}
