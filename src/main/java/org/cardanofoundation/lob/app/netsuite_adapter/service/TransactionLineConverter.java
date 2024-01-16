package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CurrencyRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.cardanofoundation.lob.app.netsuite_adapter.util.SHA3;
import org.cardanofoundation.lob.app.organisation.OrganisationApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.zeroForNull;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider.NETSUITE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLineConverter {

    private final CurrencyRepository currencyRepository;
    private final OrganisationApi organisationApi;

    public TransactionLine convert(SearchResultTransactionItem searchResultTransactionItem) {
        val organisation = organisationApi.findByForeignProvider(String.valueOf(searchResultTransactionItem.subsidiary()), NETSUITE)
                .orElseThrow();

        return new TransactionLine(
                id(organisation, searchResultTransactionItem),
                organisation.id(),
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.dateCreated(),
                searchResultTransactionItem.transactionNumber(),
                searchResultTransactionItem.number(),
                orgCurrencyPair(organisation).orElseThrow(),
                currencyPair(searchResultTransactionItem).orElseThrow(),
                searchResultTransactionItem.exchangeRate(),
                normaliseString(searchResultTransactionItem.documentNumber()),
                normaliseString(searchResultTransactionItem.id()),
                normaliseString(searchResultTransactionItem.companyName()),
                normaliseString(searchResultTransactionItem.costCenter()),
                normaliseString(searchResultTransactionItem.project()),
                vat(searchResultTransactionItem),
                normaliseString(searchResultTransactionItem.name()),
                normaliseString(searchResultTransactionItem.accountMain()),
                normaliseString(searchResultTransactionItem.memo()),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebitForeignCurrency()), zeroForNull(searchResultTransactionItem.amountCreditForeignCurrency())),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebit()), zeroForNull(searchResultTransactionItem.amountCredit()))
        );
    }

    private Optional<TransactionLine.CurrencyPair> orgCurrencyPair(Organisation organisation) {
        val organisationBaseCurrency = organisation.baseCurrency();
        val organisationBaseCurrencyId = organisationBaseCurrency.currencyId();

        return currencyRepository.findByCurrencyId(organisationBaseCurrencyId)
                .map(baseCurrency -> new TransactionLine.CurrencyPair(organisationBaseCurrency, baseCurrency));
    }

    private Optional<TransactionLine.CurrencyPair> currencyPair(SearchResultTransactionItem item) {
        val currencyInternalId = item.currency();

        return organisationApi.findOrganisationCurrencyByInternalId(currencyInternalId.toString()).flatMap(organisationCurrency -> {
            val currencyId = organisationCurrency.currencyId();

            return currencyRepository.findByCurrencyId(currencyId)
                    .map(currency -> new TransactionLine.CurrencyPair(organisationCurrency, currency));
        });
    }

    private Optional<TransactionLine.VatPair> vat(SearchResultTransactionItem searchResultTransactionItem) {
        val vatRateCodeM = normaliseString(searchResultTransactionItem.taxItem());

        if (vatRateCodeM.isEmpty()) {
            return Optional.empty();
        }

        val internalVatId = vatRateCodeM.orElseThrow();

        return organisationApi.findOrganisationVatByInternalId(internalVatId)
                .map(varOrg -> new TransactionLine.VatPair(internalVatId, varOrg.rate()));
    }

    public String id(Organisation organisation, SearchResultTransactionItem searchResultTransactionItem) {
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
