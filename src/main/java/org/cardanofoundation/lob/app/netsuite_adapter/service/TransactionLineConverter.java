package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.cardanofoundation.lob.app.netsuite_adapter.util.SHA3;
import org.cardanofoundation.lob.app.organisation.OrganisationApi;
import org.springframework.stereotype.Service;

import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.zeroForNull;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider.NETSUITE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLineConverter {

    private final CurrencyConverter currencyConverter;
    private final OrganisationApi organisationApi;

    public TransactionLine convert(SearchResultTransactionItem searchResultTransactionItem) {
        val organisationM = organisationApi.findByForeignProvider(String.valueOf(searchResultTransactionItem.subsidiary()), NETSUITE);
        val organisation = organisationM.orElseThrow();

        val currencyCode = currencyConverter.convert(searchResultTransactionItem.currency());

        return new TransactionLine(
                id(searchResultTransactionItem),
                organisation.id(),
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.dateCreated(),
                searchResultTransactionItem.transactionNumber(),
                searchResultTransactionItem.number(),
                organisation.baseCurrency().getCurrencyCode(),
                currencyCode,
                searchResultTransactionItem.exchangeRate(),
                normaliseString(searchResultTransactionItem.documentNumber()),
                normaliseString(searchResultTransactionItem.id()),
                normaliseString(searchResultTransactionItem.companyName()),
                normaliseString(searchResultTransactionItem.costCenter()),
                normaliseString(searchResultTransactionItem.project()),
                normaliseString(searchResultTransactionItem.taxItem()),
                normaliseString(searchResultTransactionItem.name()),
                normaliseString(searchResultTransactionItem.accountMain()),
                normaliseString(searchResultTransactionItem.memo()),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebitForeignCurrency()), zeroForNull(searchResultTransactionItem.amountCreditForeignCurrency())),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebit()), zeroForNull(searchResultTransactionItem.amountCredit()))
        );
    }

    public String id(SearchResultTransactionItem searchResultTransactionItem) {
        val transactionNumber  = searchResultTransactionItem.transactionNumber();

        return SHA3.digest(String.format("%s::%s::%s", NETSUITE, transactionNumber, searchResultTransactionItem.lineID()));
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
