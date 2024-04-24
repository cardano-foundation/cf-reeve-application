package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;

import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.DEBIT;
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
            processAccountCode(DEBIT, item.getAccountCodeDebit(), organisationId, item, tx);
            processAccountCode(CREDIT, item.getAccountCodeCredit(), organisationId, item, tx);

            setAccountEventCode(item);
        }
    }

    private void processAccountCode(OperationType type,
                                    Optional<String> accountCodeM,
                                    String organisationId,
                                    TransactionItemEntity item,
                                    TransactionEntity tx) {
        accountCodeM.map(String::trim)
                .filter(acc -> !acc.isEmpty())
                .ifPresent(accountCode -> {
                    val accountChartMappingM = organisationPublicApi.getChartOfAccounts(organisationId, accountCode);

                    accountChartMappingM.ifPresentOrElse(
                            chartOfAccount -> setAccountCodeRef(type, item, chartOfAccount),
                            () -> addViolation(accountCode, type, item, tx, determineSource(type))
                    );
                });
    }

    private void setAccountCodeRef(OperationType type,
                                   TransactionItemEntity item,
                                   OrganisationChartOfAccount chartOfAccount) {
        switch (type) {
            case DEBIT:
                item.setAccountCodeRefDebit(chartOfAccount.getEventRefCode());
                break;
            case CREDIT:
                item.setAccountCodeRefCredit(chartOfAccount.getEventRefCode());
                break;
        }
    }

    private void addViolation(String accountCode, OperationType type, TransactionItemEntity item, TransactionEntity tx, Violation.Source source) {
        val violation = org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation.builder()
                .txItemId(item.getId())
                .code(CHART_OF_ACCOUNT_NOT_FOUND)
                .type(ERROR)
                .source(source)
                .processorModule(this.getClass().getSimpleName())
                .bag(Map.of(
                        "accountCode", accountCode,
                        "type", type.name(),
                        "transactionNumber", tx.getTransactionInternalNumber()
                ))
                .build();

        tx.addViolation(violation);
    }

    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source determineSource(OperationType type) {
        return switch (type) {
            case DEBIT -> LOB;
            case CREDIT -> ERP;
        };
    }

    private void setAccountEventCode(TransactionItemEntity item) {
        Optional<String> accountDebitRefCode = item.getAccountCodeRefDebit();
        Optional<String> accountCreditRefCode = item.getAccountCodeRefCredit();

        if (accountDebitRefCode.isPresent() && accountCreditRefCode.isPresent()) {
            val eventCode = STR."\{accountDebitRefCode.orElseThrow()}\{accountCreditRefCode.orElseThrow()}";
            item.setAccountEventCode(eventCode);
        }
    }

}
