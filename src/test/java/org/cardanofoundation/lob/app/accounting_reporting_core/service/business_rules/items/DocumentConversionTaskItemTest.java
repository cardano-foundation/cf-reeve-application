package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCurrency;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationVat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CURRENCY_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.VAT_RATE_NOT_FOUND;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentConversionTaskItemTest {

    @Mock
    private OrganisationPublicApiIF organisationPublicApi;

    @Mock
    private CoreCurrencyRepository coreCurrencyRepository;

    private DocumentConversionTaskItem documentConversionTaskItem;

    @BeforeEach
    public void setup() {
        this.documentConversionTaskItem = new DocumentConversionTaskItem(organisationPublicApi, coreCurrencyRepository);
    }

    @Test
    public void testVatRateNotFoundAddsViolation() {
        val txId = "1";
        val organisationId = "org1";
        val customerCode = "custCode";
        val internalTransactionNumber = "INT-1";

        val transaction = Transaction.builder()
                .id(txId)
                .internalTransactionNumber(internalTransactionNumber)
                .organisation(Organisation.builder().id(organisationId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .document(Optional.of(Document.builder()
                                        .vat(Optional.of(Vat.builder()
                                                .customerCode(customerCode)
                                                .rate(Optional.empty()) // VAT rate is missing
                                                .build()))
                                        .currency(Currency.builder().customerCode("USD").build())
                                        .build()))
                                .build()))
                .build();

        when(organisationPublicApi.findOrganisationByVatAndCode(organisationId, customerCode)).thenReturn(Optional.empty());

        val result = documentConversionTaskItem.run(transaction);

        assertThat(result.getViolations()).isNotEmpty();
        assertThat(result.getViolations()).anyMatch(v -> v.code() == VAT_RATE_NOT_FOUND);
    }

    @Test
    public void testCurrencyNotFoundAddsViolation() {
        val txId = "1";
        val organisationId = "org1";
        val customerCurrencyCode = "USD";
        val vatCustomerCode = "VAT123";
        val internalTransactionNumber = "INT-1";

        val transaction = Transaction.builder()
                .id(txId)
                .internalTransactionNumber(internalTransactionNumber)
                .organisation(Organisation.builder().id(organisationId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .document(Optional.of(Document.builder()
                                        .vat(Optional.of(Vat.builder()
                                                .customerCode(vatCustomerCode)
                                                .rate(Optional.of(BigDecimal.valueOf(0.2)))
                                                .build())
                                        )
                                        .currency(Currency.builder()
                                                .customerCode(customerCurrencyCode)
                                                .coreCurrency(Optional.empty()) // Core currency is initially missing
                                                .build())
                                        .build()))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .document(Optional.of(Document.builder()
                                        .vat(Optional.of(Vat.builder()
                                                .customerCode(vatCustomerCode)
                                                .rate(Optional.of(BigDecimal.valueOf(0.2)))
                                                .build())
                                        )
                                        .currency(Currency.builder()
                                                .customerCode(customerCurrencyCode)
                                                .coreCurrency(Optional.empty()) // Core currency is initially missing
                                                .build())
                                        .build()))
                                .build())
                )

                .build();

        // Simulate currency lookup failure
        when(organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode))
                .thenReturn(Optional.empty());

        val result = documentConversionTaskItem.run(transaction);

        // Assert that a CURRENCY_NOT_FOUND violation is added
        assertThat(result.getViolations()).isNotEmpty();
        assertThat(result.getViolations()).anyMatch(v -> v.code() == CURRENCY_NOT_FOUND);
    }

    @Test
    public void testSuccessfulDocumentConversion() {
        var txId = "1";
        var organisationId = "org1";
        var customerCurrencyCode = "USD";
        var customerVatCode = "VAT123";
        var currencyId = "ISO_4217:USD";

        var transaction = Transaction.builder()
                .id(txId)
                .organisation(Organisation.builder().id(organisationId).build())
                .items(Set.of(TransactionItem.builder()
                        .document(Optional.of(Document.builder()
                                .vat(Optional.of(Vat.builder()
                                        .customerCode(customerVatCode)
                                        .rate(Optional.empty()) // Initially missing, to be enriched
                                        .build()))
                                .currency(Currency.builder()
                                        .customerCode(customerCurrencyCode)
                                        .coreCurrency(Optional.empty()) // Initially missing, to be enriched
                                        .build())
                                .build()))
                        .build()))
                .build();

        // Mock the successful VAT and Currency lookups
        when(organisationPublicApi.findOrganisationByVatAndCode(organisationId, customerVatCode))
                .thenReturn(Optional.of(OrganisationVat.builder()
                        .id(new OrganisationVat.Id(organisationId, customerVatCode))
                        .rate(BigDecimal.valueOf(0.2))
                        .build()));

        when(organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode))
                .thenReturn(Optional.of(OrganisationCurrency.builder().id(new OrganisationCurrency.Id(organisationId, customerCurrencyCode))
                        .currencyId(currencyId)
                        .build()));

        when(coreCurrencyRepository.findByCurrencyId(currencyId))
                .thenReturn(Optional.of(CoreCurrency.builder()
                        .currencyISOStandard(CoreCurrency.IsoStandard.ISO_4217)
                        .currencyISOCode("USD")
                        .name("USD Dollar")
                        .build())
                );

        var result = documentConversionTaskItem.run(transaction);

        // Assert no violations are added
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    public void testDocumentConversionWithMultipleViolations() {
        var txId = "1";
        var organisationId = "org1";
        var customerCurrencyCode = "UNKNOWN_CURRENCY";
        var customerVatCode = "UNKNOWN_VAT";
        val internalTransactionNumber = "INT-1";

        var transaction = Transaction.builder()
                .id(txId)
                .internalTransactionNumber(internalTransactionNumber)
                .organisation(Organisation.builder().id(organisationId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .document(Optional.of(Document.builder()
                                        .vat(Optional.of(Vat.builder()
                                                .customerCode(customerVatCode)
                                                .rate(Optional.empty())
                                                .build()))
                                        .currency(Currency.builder()
                                                .customerCode(customerCurrencyCode)
                                                .coreCurrency(Optional.empty())
                                                .build())
                                        .build()))
                                .build()))
                .build();

        // Simulate failures in VAT and Currency lookups
        when(organisationPublicApi.findOrganisationByVatAndCode(organisationId, customerVatCode))
                .thenReturn(Optional.empty());

        when(organisationPublicApi.findCurrencyByCustomerCurrencyCode(organisationId, customerCurrencyCode))
                .thenReturn(Optional.empty());

        var result = documentConversionTaskItem.run(transaction);

        // Assert that the correct violations are added
        assertThat(result.getViolations()).hasSize(2);
        assertThat(result.getViolations()).anyMatch(v -> v.code() == VAT_RATE_NOT_FOUND);
        assertThat(result.getViolations()).anyMatch(v -> v.code() == CURRENCY_NOT_FOUND);
    }

}
