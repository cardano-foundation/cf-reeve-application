package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.COST_CENTER_NOT_FOUND;

@RequiredArgsConstructor
@Slf4j
public class ConversionsPipelineTask implements PipelineTask {

    private final OrganisationPublicApi organisationPublicApi;

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions) {
        val passedTransactions = passedOrganisationTransactions
                .transactions().stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::vatConversion)
                .map(this::currencyCodeConversion)
                .map(this::costCenterConversion)
                .map(this::projectConversion)
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

    public Transaction.WithPossibleViolations vatConversion(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val documentM = tx.getDocument();

        if (documentM.isEmpty()) {
            return violationTransaction;
        }

        val document = documentM.orElseThrow();

        if (document.getVat().isPresent() && document.getVat().get().getRate().isEmpty()) {
            val vat = document.getVat().orElseThrow();

            val vatM = organisationPublicApi.findOrganisationVatByInternalId(tx.getOrganisation().getId(), vat.getInternalNumber());

            if (vatM.isEmpty()) {
                log.warn("VAT_RATE_NOT_FOUND: vatInternalNumber: {}", vat.getInternalNumber());

                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        Violation.Code.VAT_RATE_NOT_FOUND,
                        Map.of("vatInternalNumber", vat.getInternalNumber())
                );

                return Transaction.WithPossibleViolations.create(tx
                        .toBuilder()
                        .validationStatus(FAILED)
                        .build(), v);
            }

            val organisationVat = vatM.orElseThrow();

            val enrichedVat = Vat.builder()
                    .internalNumber(vat.getInternalNumber())
                    .rate(Optional.of(organisationVat.rate()))
                    .build();

            return Transaction.WithPossibleViolations.create(tx.toBuilder()
                    .document(Optional.of(document.toBuilder()
                            .vat(Optional.of(enrichedVat))
                            .build()))
                    .build());
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolations currencyCodeConversion(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val documentM = tx.getDocument();

        if (documentM.isEmpty()) {
            return violationTransaction;
        }

        val document = documentM.orElseThrow();

        if (document.getCurrency().getId().isEmpty()) {
            val internalNumber = document.getCurrency().getInternalNumber();
            val organisationCurrencyM = organisationPublicApi.findOrganisationCurrencyByInternalId(internalNumber);

            if (organisationCurrencyM.isEmpty()) {
                log.warn("CURRENCY_RATE_NOT_FOUND: currencyInternalId: {}", internalNumber);

                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        Violation.Code.CURRENCY_RATE_NOT_FOUND,
                        Map.of("currencyInternalNumber", internalNumber)
                );

                return Transaction.WithPossibleViolations.create(tx
                        .toBuilder()
                        .validationStatus(FAILED)
                        .build(), v);
            }

            val organisationCurrency = organisationCurrencyM.orElseThrow();

            val currency = org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                    .id(Optional.of(organisationCurrency.currencyId()))
                    .internalNumber(organisationCurrency.internalNumber())
                    .build();

            return Transaction.WithPossibleViolations.create(tx.toBuilder()
                    .document(Optional.of(document.toBuilder()
                            .currency(currency)
                            .build()))
                    .build());
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolations costCenterConversion(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val costCenterM = tx.getCostCenter();

        if (costCenterM.isEmpty()) {
            return violationTransaction;
        }

        val costCenter = costCenterM.orElseThrow();

        val organisationId = tx.getOrganisation().getId();
        val internalNumber = costCenter.getInternalNumber();
        val costCenterMappingM = organisationPublicApi.findCostCenter(organisationId, internalNumber);

        if (costCenterMappingM.isEmpty()) {
            log.warn("COST_CENTER_MAPPING_NOT_FOUND: costCenterInternalNumber: {}", internalNumber);

            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.FATAL,
                    organisationId,
                    tx.getId(),
                    COST_CENTER_NOT_FOUND,
                    Map.of("internalNumber", internalNumber)
            );

            return Transaction.WithPossibleViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        val costCenterMapping = costCenterMappingM.orElseThrow();

        return Transaction.WithPossibleViolations.create(tx.toBuilder()
                .costCenter(Optional.of(costCenter.toBuilder()
                        .externalNumber(Optional.of(costCenterMapping.externalNumber()))
                        .code(Optional.of(costCenterMapping.code()))
                        .build()))
                .build());
    }

    private Transaction.WithPossibleViolations projectConversion(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val projectM = tx.getProject();

        if (projectM.isEmpty()) {
            return violationTransaction;
        }

        val project = projectM.orElseThrow();

        val organisationId = tx.getOrganisation().getId();
        val internalNumber = project.getInternalNumber();

        val projectMappingM = organisationPublicApi.findProject(organisationId, internalNumber);

        if (projectMappingM.isEmpty()) {
            log.warn("PROJECT_CODE MAPPING NOT FOUND: internalNumber: {}", internalNumber);

            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.FATAL,
                    organisationId,
                    tx.getId(),
                    Violation.Code.PROJECT_CODE_NOT_FOUND,
                    Map.of("internalNumber", internalNumber)
            );

            return Transaction.WithPossibleViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        val projectMapping = projectMappingM.orElseThrow();

        return Transaction.WithPossibleViolations.create(tx.toBuilder()
                .project(Optional.of(project.toBuilder()
                        .code(Optional.of(projectMapping.code()))
                        .build()))
                .build());
    }

}
