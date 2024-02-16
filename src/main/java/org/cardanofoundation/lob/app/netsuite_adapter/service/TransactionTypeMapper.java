package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.Type;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@Slf4j

// TODO update this with full reference list of all types
public class TransactionTypeMapper implements Function<Type, TransactionType> {

    public TransactionType apply(Type transType) {
        return switch(transType) {
            case CardChrg -> TransactionType.CardCharge;
            case VendBill -> TransactionType.VendorBill;
            case CardRfnd -> TransactionType.CardRefund;
            case Journal -> TransactionType.Journal;
            case FxReval -> TransactionType.FxRevaluation;
            case Transfer -> TransactionType.Transfer;
            case CustPymt -> TransactionType.CustomerPayment;
            case ExpRept -> TransactionType.ExpenseReport;
            case VendPymt -> TransactionType.VendorPayment;
            case VendCred -> TransactionType.BillCredit;
        };
    }

}
