package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.DOCUMENT_MUST_BE_PRESENT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class DocumentMustBePresentTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        val violations = new HashSet<Violation>();

        for (val txItem : tx.getItems()) {
            if (txItem.getDocument().isEmpty()) {
                val v = Violation.create(
                        ERROR,
                        Violation.Source.LOB,
                        txItem.getId(),
                        DOCUMENT_MUST_BE_PRESENT,
                        this.getClass().getSimpleName(),
                        Map.of(
                                "transactionNumber", tx.getInternalTransactionNumber()
                        )
                );

                violations.add(v);
            }
        }

        if (!violations.isEmpty()) {
            return tx.toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx;
    }

}
