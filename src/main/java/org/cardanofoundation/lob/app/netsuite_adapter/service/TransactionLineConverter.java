package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreString;
import org.cardanofoundation.lob.app.netsuite_adapter.util.SHA3;
import org.cardanofoundation.lob.app.organisation.OrganisationApi;
import org.springframework.stereotype.Service;

import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.netsuite_adapter.util.MoreBigDecimal.zeroForNull;
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

        val transactionNumber  = searchResultTransactionItem.transactionNumber();

        val id = String.format("%s::%s::%s", NETSUITE, transactionNumber, searchResultTransactionItem.lineID());

        return new TransactionLine(
                SHA3.digest(id),
                organisation.id(),
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.dateCreated(),
                searchResultTransactionItem.transactionNumber(),
                searchResultTransactionItem.number(),
                organisation.baseCurrency().getCurrencyCode(),
                currencyCode,
                searchResultTransactionItem.exchangeRate(),
                MoreString.normaliseString(searchResultTransactionItem.documentNumber()),
                MoreString.normaliseString(searchResultTransactionItem.id()),
                MoreString.normaliseString(searchResultTransactionItem.companyName()),
                MoreString.normaliseString(searchResultTransactionItem.costCenter()),
                MoreString.normaliseString(searchResultTransactionItem.project()),
                MoreString.normaliseString(searchResultTransactionItem.taxItem()),
                MoreString.normaliseString(searchResultTransactionItem.name()),
                MoreString.normaliseString(searchResultTransactionItem.accountMain()),
                MoreString.normaliseString(searchResultTransactionItem.memo()),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebitForeignCurrency()), zeroForNull(searchResultTransactionItem.amountCreditForeignCurrency())),
                substractOpt(zeroForNull(searchResultTransactionItem.amountDebit()), zeroForNull(searchResultTransactionItem.amountCredit()))
                );
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
