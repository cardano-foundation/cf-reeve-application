package org.cardanofoundation.lob.app.adapter.netsuite.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.TransactionType;
import org.cardanofoundation.lob.app.adapter.netsuite.domain.SearchResultTransactionItem;
import org.cardanofoundation.lob.app.adapter.netsuite.domain.Type;
import org.springframework.stereotype.Service;

import static org.cardanofoundation.lob.app.adapter.netsuite.util.MoreBigDecimal.substractOpt;
import static org.cardanofoundation.lob.app.adapter.netsuite.util.MoreString.normaliseString;

@Service
@RequiredArgsConstructor
public class TransactionLineConverter {

    private final NetSuiteOrganisationService netSuiteOrganisationService;

    public TransactionLine convert(SearchResultTransactionItem searchResultTransactionItem) {
        val orgNameM = netSuiteOrganisationService.findOrganisationName(searchResultTransactionItem.subsidiary());

        return new TransactionLine(
                orgNameM.orElseThrow(),
                transactionType(searchResultTransactionItem.type()),
                searchResultTransactionItem.dateCreated(),
                searchResultTransactionItem.transactionNumber(),
                searchResultTransactionItem.number(),
                searchResultTransactionItem.currency(),
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
                substractOpt(searchResultTransactionItem.amountDebitForeignCurrency(), searchResultTransactionItem.amountCreditForeignCurrency()),
                substractOpt(searchResultTransactionItem.amountDebit(), searchResultTransactionItem.amountCredit())
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
