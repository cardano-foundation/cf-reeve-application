package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CHART_OF_ACCOUNT_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountEventCodesConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApi organisationPublicApi;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        val organisationId = tx.getOrganisation().getId();

        val items = tx.getItems().stream()
                .map(item -> {
                    val itemBuilder = item.toBuilder();

                    if (item.getAccountCodeDebit().isPresent()) {
                        val accountCodeDebit = item.getAccountCodeDebit().orElseThrow();

                        val accountChartMappingM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeDebit);
                        if (accountChartMappingM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    Violation.Source.LOB,
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    pipelineTask.getClass().getSimpleName(),
                                    Map.of(
                                            "accountCode", accountCodeDebit,
                                            "type", "DEBIT",
                                            "transactionNumber", tx.getInternalTransactionNumber()
                                    )
                            );

                            violations.add(v);
                        } else {
                            itemBuilder.accountCodeEventRefDebit(Optional.of(accountChartMappingM.orElseThrow().getEventRefCode()));
                        }
                    }

                    if (item.getAccountCodeCredit().isPresent()) {
                        val accountCodeCredit = item.getAccountCodeCredit().orElseThrow();

                        val eventRefCodeM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeCredit);
                        if (eventRefCodeM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    Violation.Source.LOB,
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    pipelineTask.getClass().getSimpleName(),
                                    Map.of(
                                            "accountCode", accountCodeCredit,
                                            "type", "CREDIT",
                                            "transactionNumber", tx.getInternalTransactionNumber()
                                    )
                            );

                            violations.add(v);
                        } else {
                            itemBuilder.accountCodeEventRefCredit(Optional.of(eventRefCodeM.orElseThrow().getEventRefCode()));
                        }
                    }

                    val tempItem = itemBuilder.build();

                    if (tempItem.getAccountCodeEventRefDebit().isPresent() && tempItem.getAccountCodeEventRefCredit().isPresent()) {
                        val accountDebitRefCode = tempItem.getAccountCodeEventRefDebit().orElseThrow();
                        val accountCreditRefCode = tempItem.getAccountCodeEventRefCredit().orElseThrow();

                        val eventCode = STR."\{accountDebitRefCode}\{accountCreditRefCode}";

                        itemBuilder.accountEventCode(Optional.of(eventCode));
                    }

                    return itemBuilder.build();
                })
                .collect(Collectors.toSet());

        return TransactionWithViolations
                .create(tx.toBuilder().items(items).build(),
                        violations
                );
    }

}
