package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_SANITY_CHECK_FAIL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.INTERNAL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class SanityCheckFieldsTaskItem implements PipelineTaskItem {

    private final Validator validator;

    @Override
    public Transaction run(Transaction tx) {
        val violations = new HashSet<Violation>();

        val errors = validator.validate(tx);

        if (tx.getValidationStatus() == FAILED) {
            return tx;
        }

        if (!errors.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    INTERNAL,
                    TX_SANITY_CHECK_FAIL,
                    this.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx;
    }

}
