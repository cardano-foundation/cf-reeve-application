package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Priority.NORMAL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.FATAL;

@RequiredArgsConstructor
@Slf4j
public class ConversionsPipelineTask implements PipelineTask {

    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val passedTransactions = passedOrganisationTransactions
                .transactions().stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(this::organisationConversion)
                .map(this::documentConversion)
                .map(this::costCenterConversion)
                .map(this::projectConversion)
                .map(this::accountEventCodesConversion)
                .toList();

        val newViolations = new HashSet<Violation>();
        val finalTransactions = new LinkedHashSet<Transaction>();

        for (val transactions : passedTransactions) {
            finalTransactions.add(transactions.transaction());
            newViolations.addAll(transactions.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedOrganisationTransactions.organisationId(), finalTransactions),
                ignoredOrganisationTransactions,
                newViolations
        );
    }

    private TransactionWithViolations organisationConversion(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        val organisationM = organisationPublicApi.findByOrganisationId(tx.getOrganisation().getId());

        if (organisationM.isEmpty()) {
            log.warn("ORGANISATION_NOT_FOUND: organisationId: {}", tx.getOrganisation().getId());

            val v = Violation.create(
                    NORMAL,
                    FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    ORGANISATION_NOT_FOUND,
                    Map.of("organisationId", tx.getOrganisation().getId())
            );

            return TransactionWithViolations.create(tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .build(), v);
        }

        val organisation = organisationM.orElseThrow();

        val orgVanillaCurrencyM = coreCurrencyRepository.findByCurrencyId(organisation.getCurrencyId());

        if (orgVanillaCurrencyM.isEmpty()) {
            log.warn("CURRENCY_MAPPING_NOT_FOUND: currencyId: {}", organisation.getCurrencyId());

            val v = Violation.create(
                    NORMAL,
                    FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    CORE_CURRENCY_NOT_FOUND,
                    Map.of("currencyId", organisation.getCurrencyId())
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        return TransactionWithViolations.create(tx.toBuilder()
                .organisation(tx.getOrganisation().toBuilder()
                        .shortName(Optional.of(organisation.getShortName()))
                        .currency(Optional.of(Currency.builder()
                                .coreCurrency(orgVanillaCurrencyM)
                                .customerCode(orgVanillaCurrencyM.orElseThrow().getCurrencyISOCode())
                                .build()))
                        .build())
                .build());
    }

    public TransactionWithViolations documentConversion(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();
        val organisationId = tx.getOrganisation().getId();

        val violations = new LinkedHashSet<Violation>();

        val documentM = tx.getDocument();

        if (documentM.isEmpty()) {
            return violationTransaction;
        }

        val document = documentM.orElseThrow();

        var enrichedVatM = Optional.<Vat>empty();
        var enrichedCoreCurrencyM = Optional.<CoreCurrency>empty();

        if (document.getVat().isPresent() && document.getVat().get().getRate().isEmpty()) {
            val vat = document.getVat().orElseThrow();

            val vatM = organisationPublicApi.findOrganisationByVatAndCode(organisationId, vat.getCustomerCode());

            if (vatM.isEmpty()) {
                log.warn("VAT_RATE_NOT_FOUND: vatInternalNumber: {}", vat.getCustomerCode());

                val v = Violation.create(
                        NORMAL,
                        FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        Violation.Code.VAT_RATE_NOT_FOUND,
                        Map.of("vatInternalNumber", vat.getCustomerCode())
                );

                violations.add(v);
            } else {
                val organisationVat = vatM.orElseThrow();

                enrichedVatM = Optional.of(Vat.builder()
                        .customerCode(vat.getCustomerCode())
                        .rate(Optional.of(organisationVat.getRate()))
                        .build());
            }
        }

        val customerCurrencyCode = document.getCurrency().getCustomerCode();

        val organisationCurrencyM = organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode);

        if (organisationCurrencyM.isEmpty()) {
            log.warn("CURRENCY_MAPPING_NOT_FOUND: currencyId: {}", customerCurrencyCode);

            val v = Violation.create(
                    NORMAL,
                    FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    CURRENCY_NOT_FOUND,
                    Map.of("customerCode", customerCurrencyCode)
            );

            violations.add(v);
        } else {
            val orgCurrency = organisationCurrencyM.orElseThrow();
            val currencyId = orgCurrency.getCurrencyId();
            val currencyM = coreCurrencyRepository.findByCurrencyId(currencyId);

            if (currencyM.isEmpty()) {
                log.warn("CURRENCY_MAPPING_NOT_FOUND: currencyId: {}", orgCurrency.getCurrencyId());

                val v = Violation.create(
                        NORMAL,
                        FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        CURRENCY_NOT_FOUND,
                        Map.of("currencyId", currencyId)
                );

                violations.add(v);
            } else {
                enrichedCoreCurrencyM = Optional.of(currencyM.orElseThrow());
            }
        }

        if (!violations.isEmpty()) {
            return TransactionWithViolations.create(tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .build(), violations);
        }

        return TransactionWithViolations.create(tx.toBuilder()
                .document(Optional.of(document.toBuilder()
                        .currency(document.getCurrency().toBuilder()
                                .coreCurrency(enrichedCoreCurrencyM)
                                .build())
                        .vat(enrichedVatM)
                        .build()))
                .build());
    }

    public TransactionWithViolations costCenterConversion(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val costCenterM = tx.getCostCenter();

        if (costCenterM.isEmpty()) {
            return violationTransaction;
        }

        val costCenter = costCenterM.orElseThrow();

        val organisationId = tx.getOrganisation().getId();
        val customerCode = costCenter.getCustomerCode();

        val costCenterMappingM = organisationPublicApi.findCostCenter(organisationId, customerCode);

        if (costCenterMappingM.isEmpty()) {
            log.warn("COST_CENTER_MAPPING_NOT_FOUND: costCenter customer code: {}", customerCode);

            val v = Violation.create(
                    NORMAL,
                    FATAL,
                    organisationId,
                    tx.getId(),
                    COST_CENTER_NOT_FOUND,
                    Map.of("customerCode", customerCode)
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        val costCenterMapping = costCenterMappingM.orElseThrow();

        return TransactionWithViolations.create(tx.toBuilder()
                .costCenter(Optional.of(costCenter.toBuilder()
                        .customerCode(customerCode)
                        .externalCustomerCode(Optional.of(costCenterMapping.getExternalCustomerCode()))
                        .name(Optional.of(costCenterMapping.getName()))
                        .build()))
                .build());
    }

    private TransactionWithViolations projectConversion(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val projectM = tx.getProject();

        if (projectM.isEmpty()) {
            return violationTransaction;
        }

        val project = projectM.orElseThrow();

        val organisationId = tx.getOrganisation().getId();
        val customerCode = project.getCustomerCode();

        val projectMappingM = organisationPublicApi.findProject(organisationId, customerCode);

        if (projectMappingM.isEmpty()) {
            log.warn("PROJECT_CODE MAPPING NOT FOUND: customerCode: {}", customerCode);

            val v = Violation.create(
                    NORMAL,
                    FATAL,
                    organisationId,
                    tx.getId(),
                    PROJECT_CODE_NOT_FOUND,
                    Map.of("customerCode", customerCode)
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        val projectMapping = projectMappingM.orElseThrow();

        return TransactionWithViolations.create(tx.toBuilder()
                .project(Optional.of(project.toBuilder()
                        .customerCode(projectMapping.getId().getCustomerCode())
                        .build()))
                .build());
    }

    private TransactionWithViolations accountEventCodesConversion(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        val organisationId = tx.getOrganisation().getId();

        val items = tx.getTransactionItems().stream()
                .map(item -> {
                    val itemBuilder = item.toBuilder();

                    if (item.getAccountCodeDebit().isPresent()) {
                        val accountCodeDebit = item.getAccountCodeDebit().orElseThrow();

                        val accountChartMappingM = organisationPublicApi.getChartOfAccounts(organisationId, accountCodeDebit);
                        if (accountChartMappingM.isEmpty()) {
                            log.warn("ACCOUNT_REF_CODE_MAPPING_NOT_FOUND: debit accountCode: {}", item.getAccountCodeDebit().orElseThrow());

                            val v = Violation.create(
                                    NORMAL,
                                    FATAL,
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    Map.of("accountCode", accountCodeDebit, "type", "DEBIT")
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
                            log.warn("ACCOUNT_REF_CODE_MAPPING_NOT_FOUND: credit accountCode: {}", item.getAccountCodeCredit().orElseThrow());

                            val v = Violation.create(
                                    NORMAL,
                                    FATAL,
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    CHART_OF_ACCOUNT_NOT_FOUND,
                                    Map.of("accountCode", accountCodeCredit, "type", "CREDIT")
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
                .create(tx.toBuilder().transactionItems(items).build(),
                        violations
                );
    }

}
