package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;

import java.util.HashSet;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TRANSACTION_ITEMS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class NoTransactionItemsTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;

    @Override
    public TransactionWithViolations run(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getItems().isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    Violation.Source.ERP,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    TRANSACTION_ITEMS_EMPTY,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of("transactionNumber", tx.getInternalTransactionNumber())
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

        return transactionWithViolations;
    }

}
