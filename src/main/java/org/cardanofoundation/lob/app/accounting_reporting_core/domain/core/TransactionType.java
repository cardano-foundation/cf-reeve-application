package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum TransactionType {

    CardCharge(1),
    VendorBill(2),
    CardRefund(4),
    Journal(8),
    FxRevaluation(16),
    Transfer(32),
    CustomerPayment(64),
    ExpenseReport(128),
    VendorPayment(256),
    BillCredit(512);

    private int value;

    TransactionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
