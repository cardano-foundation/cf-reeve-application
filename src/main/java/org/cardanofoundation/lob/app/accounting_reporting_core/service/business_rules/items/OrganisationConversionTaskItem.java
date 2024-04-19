package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CORE_CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ORGANISATION_DATA_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class OrganisationConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public void run(TransactionEntity tx) {
        val organisationId = tx.getOrganisation().getId();
        val organisationM = organisationPublicApi.findByOrganisationId(organisationId);

        if (organisationM.isEmpty()) {
            val v = Violation.builder()
                    .code(ORGANISATION_DATA_NOT_FOUND)
                    .txItemId(tx.getId())
                    .type(ERROR)
                    .source(LOB)
                    .processorModule(this.getClass().getSimpleName())
                    .bag(
                            Map.of(
                                    "transactionNumber", tx.getTransactionInternalNumber()
                            )
                    )
                    .build();

            tx.addViolation(v);
        }

        val organisation = organisationM.orElseThrow();

        val coreCurrencyM = coreCurrencyRepository.findByCurrencyId(organisation.getCurrencyId());

        if (coreCurrencyM.isEmpty()) {
            val v = Violation.builder()
                    .code(CORE_CURRENCY_NOT_FOUND)
                    .txItemId(tx.getId())
                    .type(ERROR)
                    .source(LOB)
                    .processorModule(this.getClass().getSimpleName())
                    .bag(
                            Map.of(
                                    "currencyId", organisation.getCurrencyId(),
                                    "transactionNumber", tx.getTransactionInternalNumber()
                            )
                    )
                    .build();

            tx.addViolation(v);
        }

        tx.setTransactionApproved(organisation.isPreApproveTransactionsEnabled());
        tx.setLedgerDispatchApproved(organisation.isPreApproveTransactionsDispatchEnabled());
        tx.setOrganisation(tx.getOrganisation().toBuilder()
                .shortName(organisation.getShortName())
                .currencyId(organisation.getCurrencyId())
                .build()
        );
    }

}
