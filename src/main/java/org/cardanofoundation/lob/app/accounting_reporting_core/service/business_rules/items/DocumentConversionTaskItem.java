package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class DocumentConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();
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
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    Violation.Code.VAT_RATE_NOT_FOUND,
                                    pipelineTask.getClass().getSimpleName(),
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

                    val organisationCurrencyM = organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode);

                    if (organisationCurrencyM.isEmpty()) {
                        val v = Violation.create(
                                ERROR,
                                tx.getOrganisation().getId(),
                                tx.getId(),
                                CURRENCY_NOT_FOUND,
                                pipelineTask.getClass().getSimpleName(),
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
                                    tx.getOrganisation().getId(),
                                    tx.getId(),
                                    CURRENCY_NOT_FOUND,
                                    pipelineTask.getClass().getSimpleName(),
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
            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    violations
            );
        }

        return TransactionWithViolations.create(tx.toBuilder()
                .items(txItems)
                .build());
    }

}
