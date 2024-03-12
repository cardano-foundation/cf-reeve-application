package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CORE_CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ORGANISATION_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class OrganisationConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public TransactionWithViolations run(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        val organisationId = tx.getOrganisation().getId();
        val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

        if (organisationM.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    organisationId,
                    tx.getId(),
                    ORGANISATION_NOT_FOUND,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            return TransactionWithViolations.create(tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .build(), v);
        }

        val organisation = organisationM.orElseThrow();

        val orgVanillaCurrencyM = coreCurrencyRepository.findByCurrencyId(organisation.getCurrencyId());

        if (orgVanillaCurrencyM.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    organisationId,
                    tx.getId(),
                    CORE_CURRENCY_NOT_FOUND,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "currencyId", organisation.getCurrencyId(),
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
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

}
