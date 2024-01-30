package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

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

    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations
    ) {
        val converted = passedTransactionLines.entries().stream()
                .map(TransactionLine.WithPossibleViolation::create)
                .map(this::vatConversion)
                .map(this::currencyCode)
                .toList();

        val newViolations = converted.stream()
                .filter(p -> p.violation().isPresent())
                .map(p -> p.violation().orElseThrow())
                .collect(toSet());

        val passedTxLines = converted.stream()
                .map(TransactionLine.WithPossibleViolation::transactionLine)
                .toList();

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passedTxLines),
                ignoredTransactionLines,
                filteredTransactionLines,
                Stream.concat(violations.stream(), newViolations.stream()).collect(toSet())
        );
    }

    public TransactionLine.WithPossibleViolation vatConversion(TransactionLine.WithPossibleViolation violationTransactionLine) {
        val transactionLine = violationTransactionLine.transactionLine();

        if (transactionLine.getVatInternalCode().isPresent() && transactionLine.getVatRate().isEmpty()) {
            val vatInternalCode = transactionLine.getVatInternalCode().get();

            val vatM = organisationPublicApi.findOrganisationVatByInternalId(vatInternalCode);

            if (vatM.isEmpty()) {
                log.warn("VAT_RATE_NOT_FOUND: {}", vatInternalCode);

                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "VAT_RATE_NOT_FOUND",
                        Map.of("vatInternalCode", vatInternalCode)
                );

                return TransactionLine.WithPossibleViolation.create(transactionLine
                        .toBuilder()
                        .validationStatus(FAILED)
                        .build(),
                        v);
            }

            val vat = vatM.get();

            return TransactionLine.WithPossibleViolation
                    .create(transactionLine.toBuilder().vatRate(Optional.of(vat.rate()))
                            .build(), violationTransactionLine.violation());
        }

        return violationTransactionLine;
    }

    public TransactionLine.WithPossibleViolation currencyCode(TransactionLine.WithPossibleViolation violationTransactionLine) {
        val transactionLine = violationTransactionLine.transactionLine();

        if (transactionLine.getTargetCurrencyId().isEmpty()) {
            val targetCurrencyInternalId = transactionLine.getTargetCurrencyInternalId();

            val organisationCurrencyByInternalIdM = organisationPublicApi.findOrganisationCurrencyByInternalId(targetCurrencyInternalId);

            if (organisationCurrencyByInternalIdM.isEmpty()) {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "CURRENCY_RATE_NOT_FOUND",
                        Map.of("currencyInternalId", targetCurrencyInternalId)
                );

                return TransactionLine.WithPossibleViolation
                        .create(transactionLine.toBuilder()
                                .validationStatus(FAILED)
                                .build(),
                                v);
            }
            val organisationCurrencyByInternalId = organisationCurrencyByInternalIdM.orElseThrow();

            return TransactionLine.WithPossibleViolation.create(transactionLine
                    .toBuilder()
                    .targetCurrencyId(Optional.of(organisationCurrencyByInternalId.currencyId()))
                    .build(), violationTransactionLine.violation());
        }

        return violationTransactionLine;
    }

}
