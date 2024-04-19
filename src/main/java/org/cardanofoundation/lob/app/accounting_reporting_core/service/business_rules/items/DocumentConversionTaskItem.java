package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.LOB;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class DocumentConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public void run(TransactionEntity tx) {
        val organisationId = tx.getOrganisation().getId();

        for (val txItem : tx.getItems()) {
            val documentM = txItem.getDocument();

            if (documentM.isEmpty()) {
                continue;
            }

            val document = documentM.orElseThrow();

            var enrichedVatM = Optional.<Vat>empty();
            var enrichedCoreCurrencyM = Optional.<CoreCurrency>empty();

            if (document.getVat().isPresent() && document.getVat().get().getRate().isEmpty()) {
                val vat = document.getVat().orElseThrow();

                val vatM = organisationPublicApi.findOrganisationByVatAndCode(organisationId, vat.getCustomerCode());

                if (vatM.isEmpty()) {
                    val v = Violation.builder()
                            .txItemId(txItem.getId())
                            .code(VAT_DATA_NOT_FOUND)
                            .type(ERROR)
                            .source(LOB)
                            .processorModule(this.getClass().getSimpleName())
                            .bag(
                                    Map.of(
                                            "customerCode", vat.getCustomerCode(),
                                            "transactionNumber", tx.getTransactionInternalNumber()
                                    )
                            )
                            .build();

                    tx.addViolation(v);
                } else {
                    val organisationVat = vatM.orElseThrow();

                    enrichedVatM = Optional.of(Vat.builder()
                            .customerCode(vat.getCustomerCode())
                            .rate(Optional.of(organisationVat.getRate()))
                            .build());
                }
            }

            val customerCurrencyCode = document.getCurrency().getCustomerCode();
            if (StringUtils.isBlank(customerCurrencyCode)) {
                continue;
            }

            val organisationCurrencyM = organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode);

            if (organisationCurrencyM.isEmpty()) {
                val v = Violation.builder()
                        .code(CURRENCY_DATA_NOT_FOUND)
                        .txItemId(txItem.getId())
                        .type(ERROR)
                        .source(LOB)
                        .processorModule(this.getClass().getSimpleName())
                        .bag(
                                Map.of(
                                        "customerCode", customerCurrencyCode,
                                        "transactionNumber", tx.getTransactionInternalNumber()
                                ))
                        .build();

                tx.addViolation(v);
            } else {
                val orgCurrency = organisationCurrencyM.orElseThrow();
                val currencyId = orgCurrency.getCurrencyId();
                val currencyM = coreCurrencyRepository.findByCurrencyId(currencyId);

                if (currencyM.isEmpty()) {
                    val v = Violation.builder()
                            .txItemId(txItem.getId())
                            .code(CORE_CURRENCY_NOT_FOUND)
                            .type(ERROR)
                            .source(LOB)
                            .processorModule(this.getClass().getSimpleName())
                            .bag(
                                    Map.of(
                                            "currencyId", currencyId,
                                            "transactionNumber", tx.getTransactionInternalNumber()
                                    )
                            )
                            .build();

                    tx.addViolation(v);
                } else {
                    enrichedCoreCurrencyM = Optional.of(currencyM.orElseThrow());
                }
            }

            val finalEnrichedVatM = enrichedVatM;

            txItem.setDocument(document.toBuilder()
                    .currency(document.getCurrency().toBuilder()
                            .id(enrichedCoreCurrencyM.map(CoreCurrency::toExternalId).orElse(null))
                            .build())
                    .vat(getVat(document, finalEnrichedVatM)).build()
            );
        }
    }

    @Nullable
    private static org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat getVat(Document document,
                                                                                                    Optional<Vat> finalEnrichedVatM) {
        if (finalEnrichedVatM.isEmpty() || document.getVat().isEmpty()) {
            return null;
        }

        if (finalEnrichedVatM.orElseThrow().getRate().isEmpty()) {
            return null;
        }

        val vatRate = finalEnrichedVatM.orElseThrow().getRate().orElseThrow();

        return document.getVat().map(v -> v.toBuilder()
                        .rate(vatRate)
                        .build())
                .orElse(null);
    }

}
