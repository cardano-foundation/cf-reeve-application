package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public enum RejectionStatus {

    REJECTED,
    INCORRECT_AMOUNT,
    INCORRECT_COST_CENTER,
    INCORRECT_PROJECT,
    INCORRECT_CURRENCY,
    INCORRECT_VAT_CODE,
    REVIEW_PARENT_COST_CENTER,
    REVIEW_PARENT_PROJECT_CODE,
    NOT_REJECTED;

    public boolean isRejected() {
        return this != NOT_REJECTED;
    }

}
