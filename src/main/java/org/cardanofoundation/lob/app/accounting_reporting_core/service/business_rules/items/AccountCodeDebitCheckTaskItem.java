package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_DEBIT_IS_EMPTY;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountCodeDebitCheckTaskItem implements PipelineTaskItem {

    @Override
    public void run(TransactionEntity tx) {
        if (tx.getTransactionType() == FxRevaluation) {
            return;
        }

        for (val txItem : tx.getItems()) {
            if (txItem.getAccountCodeDebit().map(String::trim).filter(code -> !code.isEmpty()).isEmpty()) {
                val v = Violation.builder()
                        .code(ACCOUNT_CODE_DEBIT_IS_EMPTY)
                        .txItemId(txItem.getId())
                        .type(ERROR)
                        .source(ERP)
                        .processorModule(this.getClass().getSimpleName())
                        .bag(Map.of("transactionNumber", tx.getTransactionInternalNumber()))
                        .build();

                tx.addViolation(v);
            }
        }
    }

}
