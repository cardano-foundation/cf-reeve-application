package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.FCY_BALANCE_MUST_BE_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AmountFcyBalanceZerosOutCheckTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        val txItems = tx.getItems();

        val fcySum = txItems.stream()
                .map(TransactionItem::getAmountFcy)
                .reduce(ZERO, BigDecimal::add);

        if (fcySum.signum() != 0) {
            val v = Violation.create(
                    ERROR,
                    Violation.Source.ERP,
                    FCY_BALANCE_MUST_BE_ZERO,
                    this.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            return tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), Set.of(v).stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx;
    }

}
