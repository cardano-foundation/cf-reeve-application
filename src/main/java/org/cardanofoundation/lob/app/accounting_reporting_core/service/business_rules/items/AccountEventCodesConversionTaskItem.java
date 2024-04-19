package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CHART_OF_ACCOUNT_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountEventCodesConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public void run(TransactionEntity tx) {
        val organisationId = tx.getOrganisation().getId();

        for (val item : tx.getItems()) {
            if (item.getAccountCodeDebit().map(String::trim).filter(acc -> !acc.isEmpty()).isPresent()) {
                val accountCodeDebit = item.getAccountCodeDebit().orElseThrow();

                val accountChartMappingM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeDebit);

                if (accountChartMappingM.isEmpty()) {
                    val v = Violation.builder()
                            .code(CHART_OF_ACCOUNT_NOT_FOUND)
                            .txItemId(item.getId())
                            .type(ERROR)
                            .source(LOB)
                            .processorModule(this.getClass().getSimpleName())
                            .bag(
                                    Map.of(
                                            "accountCode", accountCodeDebit,
                                            "type", "DEBIT",
                                            "transactionNumber", tx.getTransactionInternalNumber()
                                    )
                            )
                            .build();

                    tx.addViolation(v);
                } else {
                    item.setAccountCodeRefDebit(accountChartMappingM.map(OrganisationChartOfAccount::getEventRefCode).orElse(null));
                }
            }

            if (item.getAccountCodeCredit().map(String::trim).filter(acc -> !acc.isEmpty()).isPresent()) {
                val accountCodeCredit = item.getAccountCodeCredit().orElseThrow();

                val eventRefCodeM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeCredit);
                if (eventRefCodeM.isEmpty()) {

                    val v = Violation.builder()
                            .txItemId(item.getId())
                            .code(CHART_OF_ACCOUNT_NOT_FOUND)
                            .type(ERROR)
                            .source(ERP)
                            .processorModule(this.getClass().getSimpleName())
                            .bag(
                                    Map.of(
                                            "accountCode", accountCodeCredit,
                                            "type", "CREDIT",
                                            "transactionNumber", tx.getTransactionInternalNumber()
                                    )
                            )
                            .build();

                    tx.addViolation(v);
                } else {
                    item.setAccountCodeRefCredit(eventRefCodeM.map(OrganisationChartOfAccount::getEventRefCode).orElse(null));
                }
            }

            if (item.getAccountCodeRefDebit().isPresent() && item.getAccountCodeRefCredit().isPresent()) {
                val accountDebitRefCode = item.getAccountCodeRefDebit().orElseThrow();
                val accountCreditRefCode = item.getAccountCodeRefCredit().orElseThrow();

                val eventCode = STR."\{accountDebitRefCode}\{accountCreditRefCode}";

                item.setAccountEventCode(eventCode);
            }
        }
    }

}
