package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CORE_CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ORGANISATION_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class OrganisationConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public Transaction run(Transaction tx) {
        val organisationId = tx.getOrganisation().getId();
        val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

        val violations = new LinkedHashSet<Violation>();

        if (organisationM.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    Violation.Source.LOB,
                    ORGANISATION_NOT_FOUND,
                    this.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            violations.add(v);
        }

        val organisation = organisationM.orElseThrow();

        val coreCurrencyM = coreCurrencyRepository.findByCurrencyId(organisation.getCurrencyId());

        if (coreCurrencyM.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    Violation.Source.INTERNAL,
                    CORE_CURRENCY_NOT_FOUND,
                    this.getClass().getSimpleName(),
                    Map.of(
                            "currencyId", organisation.getCurrencyId(),
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx.toBuilder()
                .transactionApproved(organisation.isPreApproveTransactionsEnabled())
                .ledgerDispatchApproved(organisation.isPreApproveTransactionsDispatchEnabled())
                .organisation(tx.getOrganisation().toBuilder()
                        .shortName(Optional.of(organisation.getShortName()))
                        .currency(Optional.of(Currency.builder()
                                .coreCurrency(coreCurrencyM)
                                .customerCode(coreCurrencyM.orElseThrow().getCurrencyISOCode())
                                .build()))
                        .build())
                .build();
    }

}
