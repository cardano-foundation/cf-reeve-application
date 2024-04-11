package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CHART_OF_ACCOUNT_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class AccountEventCodesConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public Transaction run(Transaction tx) {
        val violations = new HashSet<Violation>();

        val organisationId = tx.getOrganisation().getId();

        val items = tx.getItems().stream()
                .map(item -> {
                    val itemBuilder = item.toBuilder();

                    if (item.getAccountCodeDebit().map(String::trim).filter(acc -> !acc.isEmpty()).isPresent()) {
                        val accountCodeDebit = item.getAccountCodeDebit().orElseThrow();

                        val accountChartMappingM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeDebit);
                        if (accountChartMappingM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    LOB,
                                    item.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    this.getClass().getSimpleName(),
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

                    if (item.getAccountCodeCredit().map(String::trim).filter(acc -> !acc.isEmpty()).isPresent()) {
                        val accountCodeCredit = item.getAccountCodeCredit().orElseThrow();

                        val eventRefCodeM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeCredit);
                        if (eventRefCodeM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    LOB,
                                    item.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    this.getClass().getSimpleName(),
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

        if (!violations.isEmpty()) {
            return tx.toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx.toBuilder()
                .items(items)
                .build();
    }

}
