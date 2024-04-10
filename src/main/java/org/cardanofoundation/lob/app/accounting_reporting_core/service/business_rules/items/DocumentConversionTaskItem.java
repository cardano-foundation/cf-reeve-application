package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.VAT_RATE_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class DocumentConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public Transaction run(Transaction tx) {
        val organisationId = tx.getOrganisation().getId();

        val violations = new LinkedHashSet<Violation>();

        val txItems = tx.getItems().stream().map(txItem -> {
                    val documentM = txItem.getDocument();

                    if (documentM.isEmpty()) {
                        return txItem;
                    }

                    val document = documentM.orElseThrow();

                    var enrichedVatM = Optional.<Vat>empty();
                    var enrichedCoreCurrencyM = Optional.<CoreCurrency>empty();

                    if (document.getVat().isPresent() && document.getVat().get().getRate().isEmpty()) {
                        val vat = document.getVat().orElseThrow();

                        val vatM = organisationPublicApi.findOrganisationByVatAndCode(organisationId, vat.getCustomerCode());

                        if (vatM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    Violation.Source.LOB,
                                    txItem.getId(),
                                    VAT_RATE_NOT_FOUND,
                                    this.getClass().getSimpleName(),
                                    Map.of(
                                            "customerCode", vat.getCustomerCode(),
                                            "transactionNumber", tx.getInternalTransactionNumber()
                                    )
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
                    if (StringUtils.isBlank(customerCurrencyCode)) {
                        return txItem;
                    }

                    val organisationCurrencyM = organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode);

                    if (organisationCurrencyM.isEmpty()) {
                        val v = Violation.create(
                                ERROR,
                                Violation.Source.LOB,
                                txItem.getId(),
                                CURRENCY_NOT_FOUND,
                                this.getClass().getSimpleName(),
                                Map.of(
                                        "customerCode", customerCurrencyCode,
                                        "transactionNumber", tx.getInternalTransactionNumber()
                                )
                        );

                        violations.add(v);
                    } else {
                        val orgCurrency = organisationCurrencyM.orElseThrow();
                        val currencyId = orgCurrency.getCurrencyId();
                        val currencyM = coreCurrencyRepository.findByCurrencyId(currencyId);

                        if (currencyM.isEmpty()) {
                            val v = Violation.create(
                                    ERROR,
                                    Violation.Source.LOB,
                                    txItem.getId(),
                                    CURRENCY_NOT_FOUND,
                                    this.getClass().getSimpleName(),
                                    Map.of(
                                            "currencyId", currencyId,
                                            "transactionNumber", tx.getInternalTransactionNumber()
                                    )
                            );

                            violations.add(v);
                        } else {
                            enrichedCoreCurrencyM = Optional.of(currencyM.orElseThrow());
                        }
                    }

                    return txItem.toBuilder()
                            .document(Optional.of(document.toBuilder()
                                    .currency(document.getCurrency().toBuilder()
                                            .coreCurrency(enrichedCoreCurrencyM)
                                            .build())
                                    .vat(enrichedVatM)
                                    .build()))
                            .build();
                })
                .collect(Collectors.toSet());

        if (!violations.isEmpty()) {
            return tx
                    .toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx.toBuilder()
                .items(txItems)
                .build();
    }

}
