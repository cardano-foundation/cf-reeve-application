package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.cardanofoundation.lob.app.netsuite_adapter.util.SHA3;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.zeroForNull;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;
import static org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider.NETSUITE;

@Service("netsuite_adapter.TransactionLineConverter")
@RequiredArgsConstructor
@Slf4j
public class TransactionLineConverter {

    private final OrganisationPublicApi organisationPublicApi;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TransactionLine convert(SearchResultTransactionItem searchResultTransactionItem) {
        val organisation = organisationPublicApi.findByForeignProvider(String.valueOf(searchResultTransactionItem.subsidiary()), NETSUITE)
                .orElseThrow();

        return new TransactionLine(
                createTxLineId(organisation, searchResultTransactionItem),
                organisation.id(),
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.dateCreated(),
                searchResultTransactionItem.transactionNumber(),
                searchResultTransactionItem.number(),
                covertOrganisationCurrency(organisation).orElseThrow(),
                convertCurrency(searchResultTransactionItem).orElseThrow(),
                searchResultTransactionItem.exchangeRate(),
                TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED,
                normaliseString(searchResultTransactionItem.documentNumber()),
                normaliseString(searchResultTransactionItem.id()),
                normaliseString(searchResultTransactionItem.companyName()),
                normaliseString(searchResultTransactionItem.costCenter()),
                normaliseString(searchResultTransactionItem.project()),
                convertVat(searchResultTransactionItem),
                normaliseString(searchResultTransactionItem.name()),
                normaliseString(searchResultTransactionItem.accountMain()),
                normaliseString(searchResultTransactionItem.memo()),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebitForeignCurrency()), zeroForNull(searchResultTransactionItem.amountCreditForeignCurrency())),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebit()), zeroForNull(searchResultTransactionItem.amountCredit()))
        );
    }

    private Optional<TransactionLine.CurrencyPair> covertOrganisationCurrency(Organisation organisation) {
        val organisationBaseCurrency = organisation.baseCurrency();
        val organisationBaseCurrencyId = organisationBaseCurrency.currencyId();

        return organisationPublicApi.findByCurrencyId(organisationBaseCurrencyId)
                .map(baseCurrency -> new TransactionLine.CurrencyPair(organisationBaseCurrency, baseCurrency));
    }

    private Optional<TransactionLine.CurrencyPair> convertCurrency(SearchResultTransactionItem item) {
        val currencyInternalId = item.currency();

        return organisationPublicApi.findOrganisationCurrencyByInternalId(currencyInternalId.toString()).flatMap(organisationCurrency -> {
            val currencyId = organisationCurrency.currencyId();

            return organisationPublicApi.findByCurrencyId(currencyId)
                    .map(currency -> new TransactionLine.CurrencyPair(organisationCurrency, currency));
        });
    }

    private Optional<TransactionLine.VatPair> convertVat(SearchResultTransactionItem searchResultTransactionItem) {
        val vatRateCodeM = normaliseString(searchResultTransactionItem.taxItem());

        if (vatRateCodeM.isEmpty()) {
            return Optional.empty();
        }

        val internalVatId = vatRateCodeM.orElseThrow();

        val organisationVatM = organisationPublicApi.findOrganisationVatByInternalId(internalVatId);

        if (organisationVatM.isEmpty()) {
            log.error("Vat organisationVat not found for internalVatId: {}", internalVatId);

            applicationEventPublisher.publishEvent(NotificationEvent.create(
                    ERROR,
                    "Vat Rate not found for internalVatId: " + internalVatId));

            // TODO we need this to return Either or go with exceptions
            //throw new RuntimeException("Vat Rate not found for internalVatId: " + internalVatId);

            return Optional.empty();
        }

        val organisationVat = organisationVatM.orElseThrow();

        return Optional.of(new TransactionLine.VatPair(internalVatId, organisationVat.rate()));
    }

    public String createTxLineId(Organisation organisation, SearchResultTransactionItem searchResultTransactionItem) {
        val transactionNumber  = searchResultTransactionItem.transactionNumber();

        return SHA3.digest(String.format("%s::%s::%s::%s", NETSUITE, organisation.id(), transactionNumber, searchResultTransactionItem.lineID()));
    }

    private TransactionType transactionType(Type transType) {
        return switch(transType) {
            case CardChrg -> TransactionType.CardCharge;
            case VendBill -> TransactionType.VendorBill;
            case CardRfnd -> TransactionType.CardRefund;
            case Journal -> TransactionType.Journal;
            case FxReval -> TransactionType.FxRevaluation;
            case Transfer -> TransactionType.Transfer;
            case CustPymt -> TransactionType.CustomerPayment;
        };
    }

}
