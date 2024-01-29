package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversionsService {

    private final OrganisationPublicApi organisationPublicApi;

    public TransformationResult run(TransactionLines transactionLines, TransactionLines ignoredTransactionLines) {
        val converted = transactionLines.entries().stream()
                .map(Either::<Violation, TransactionLine>right)
                .map(this::vatConversion)
                .map(this::currencyCode)
                .toList();

        val violations = converted.stream().filter(Either::isLeft).map(Either::getLeft).collect(Collectors.toSet());
        val convertedTxLines = converted.stream().filter(Either::isRight).map(Either::get).toList();

        val successfulTransactionLines = new TransactionLines(transactionLines.organisationId(), convertedTxLines);

        return TransformationResult.create(successfulTransactionLines, ignoredTransactionLines, violations);
    }

    public Either<Violation, TransactionLine> vatConversion(Either<Violation, TransactionLine> transactionLineE) {
        return transactionLineE.flatMap(transactionLine -> {
            if (transactionLine.getVatInternalCode().isPresent() && transactionLine.getVatRate().isEmpty()) {
                val vatInternalCode = transactionLine.getVatInternalCode().get();

                val vatM = organisationPublicApi.findOrganisationVatByInternalId(vatInternalCode);
                if (vatM.isEmpty()) {
                    return Either.left(Violation.create(
                            transactionLine.getId(),
                            Violation.Priority.NORMAL,
                            transactionLine.getInternalTransactionNumber(),
                            "VAT_RATE_NOT_FOUND",
                            Map.of("vatInternalCode", vatInternalCode)
                    ));
                }

                val vat = vatM.get();

                return Either.right(transactionLine.toBuilder().vatRate(Optional.of(vat.rate())).build());
            }

            return Either.right(transactionLine);
        });
    }

    public Either<Violation, TransactionLine> currencyCode(Either<Violation, TransactionLine> transactionLineE) {
        return transactionLineE.flatMap(transactionLine -> {
            if (transactionLine.getTargetCurrencyId().isEmpty()) {
                val targetCurrencyInternalId = transactionLine.getTargetCurrencyInternalId();

                val organisationCurrencyByInternalIdM = organisationPublicApi.findOrganisationCurrencyByInternalId(targetCurrencyInternalId);

                if (organisationCurrencyByInternalIdM.isEmpty()) {
                    return Either.left(Violation.create(
                            transactionLine.getId(),
                            Violation.Priority.NORMAL,
                            transactionLine.getInternalTransactionNumber(),
                            "CURRENCY_RATE_NOT_FOUND",
                            Map.of("currencyInternalId", targetCurrencyInternalId)
                    ));
                }
                val organisationCurrencyByInternalId = organisationCurrencyByInternalIdM.get();

                return Either.right(transactionLine.toBuilder().targetCurrencyId(Optional.of(organisationCurrencyByInternalId.currencyId())).build());
            }

            return Either.right(transactionLine);
        });
    }

}
