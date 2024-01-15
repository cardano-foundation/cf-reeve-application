package org.cardanofoundation.lob.app.adapter.netsuite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.TransactionType;
import org.cardanofoundation.lob.app.adapter.netsuite.domain.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.adapter.netsuite.domain.Type;
import org.cardanofoundation.lob.app.organisation.OrganisationApi;
import org.springframework.stereotype.Service;

import static org.cardanofoundation.lob.app.adapter.netsuite.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.adapter.netsuite.util.MoreBigDecimal.zeroForNull;
import static org.cardanofoundation.lob.app.adapter.netsuite.util.MoreString.normaliseString;
import static org.cardanofoundation.lob.app.organisation.domain.AccountSystemProvider.NETSUITE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLineConverter {

    private final CurrencyConverter currencyConverter;
    private final OrganisationApi organisationApi;

    public TransactionLine convert(SearchResultTransactionItem searchResultTransactionItem) {
        val orgM = organisationApi.findByForeignProvider(String.valueOf(searchResultTransactionItem.subsidiary()), NETSUITE);
        val organisation = orgM.orElseThrow();

        val currencyCode = currencyConverter.convert(searchResultTransactionItem.currency());

        log.info("currency:{}", searchResultTransactionItem.currency());

        return new TransactionLine(
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
