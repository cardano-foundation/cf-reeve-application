package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_DEBIT_IS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountCodeDebitCheckTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        val violations = new HashSet<Violation>();

        if (tx.getTransactionType() == FxRevaluation) {
            return tx;
        }

        for (val txItem : tx.getItems()) {
            if (txItem.getAccountCodeDebit().map(String::trim).filter(code -> !code.isEmpty()).isEmpty())  {
                val v = Violation.create(
                        ERROR,
                        LOB,
                        txItem.getId(),
                        ACCOUNT_CODE_DEBIT_IS_EMPTY,
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
