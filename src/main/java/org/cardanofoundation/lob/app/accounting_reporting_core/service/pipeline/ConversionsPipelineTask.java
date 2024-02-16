package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@RequiredArgsConstructor
@Slf4j
public class ConversionsPipelineTask implements PipelineTask {

    private final OrganisationPublicApi organisationPublicApi;

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> violations
    ) {
        val passedTransactions = passedOrganisationTransactions.transactions().stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::vatConversion)
                .map(this::currencyCode)
                .toList();

        val newViolations = new HashSet<>(violations);
        val finalTransactions = new HashSet<Transaction>();

        for (val transaction : passedTransactions) {
            finalTransactions.add(transaction.transaction());
            newViolations.addAll(transaction.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedOrganisationTransactions.organisationId(), finalTransactions),
                ignoredOrganisationTransactions,
                Stream.concat(violations.stream(), newViolations.stream()).collect(toSet())
        );
    }

    public Transaction.WithPossibleViolations vatConversion(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        if (tx.getDocument().getVat().isPresent() && tx.getDocument().getVat().get().getRate().isEmpty()) {

            val vat = tx.getDocument().getVat().orElseThrow();

            val vatM = organisationPublicApi.findOrganisationVatByInternalId(tx.getOrganisation().getId(), vat.getInternalNumber());

            if (vatM.isEmpty()) {
                log.warn("VAT_RATE_NOT_FOUND: vatInternalNumber: {}", vat.getInternalNumber());

                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        "VAT_RATE_NOT_FOUND",
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
                    .document(tx.getDocument().toBuilder()
                            .vat(Optional.of(enrichedVat))
                            .build())
                    .build());
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolations currencyCode(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        if (tx.getDocument().getCurrency().getId().isEmpty()) {
            val internalNumber = tx.getDocument().getCurrency().getInternalNumber();
            val organisationCurrencyM = organisationPublicApi.findOrganisationCurrencyByInternalId(internalNumber);

            if (organisationCurrencyM.isEmpty()) {
                log.warn("CURRENCY_RATE_NOT_FOUND: currencyInternalId: {}", internalNumber);

                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        "CURRENCY_RATE_NOT_FOUND",
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
                    .document(tx.getDocument().toBuilder()
                            .currency(currency)
                            .build())
                    .build());
        }

        return violationTransaction;
    }

}
