package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;


import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.LCY_BALANCE_MUST_BE_ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AmountLcyBalanceZerosOutCheckTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        val txItems = tx.getItems();
        val lcySum = txItems.stream().map(TransactionItem::getAmountLcy).reduce(ZERO, BigDecimal::add);

        if (lcySum.signum() != 0) {
            val v = Violation.create(
                    ERROR,
                    ERP,
                    LCY_BALANCE_MUST_BE_ZERO,
                    this.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            return tx.toBuilder()
                            .validationStatus(FAILED)
                            .violations(Stream.concat(tx.getViolations().stream(), Stream.of(v)).collect(Collectors.toSet()))
                            .build();
        }

        return tx;
    }

}
