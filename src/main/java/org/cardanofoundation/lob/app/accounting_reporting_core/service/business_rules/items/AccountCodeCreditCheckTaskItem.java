package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_CREDIT_IS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountCodeCreditCheckTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        if (tx.getTransactionType() == Journal) {
            return tx;
        }

        val violations = new HashSet<Violation>();

        for (val txItem : tx.getItems()) {
            if (txItem.getAccountCodeCredit().map(String::trim).filter(acc -> !acc.isEmpty()).isEmpty())  {
                val v = Violation.create(
                        ERROR,
                        Violation.Source.LOB,
                        txItem.getId(),
                        ACCOUNT_CODE_CREDIT_IS_EMPTY,
                        this.getClass().getSimpleName(),
                        Map.of("transactionNumber", tx.getInternalTransactionNumber())
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
