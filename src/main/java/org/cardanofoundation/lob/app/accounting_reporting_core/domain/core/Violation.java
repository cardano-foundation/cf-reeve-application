package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Priority priority,
                        Type type,
                        String organisationId,
                        String transactionId,
                        Optional<String> txItemId,
                        Violation.Code violationCode,
                        Map<String, Object> bag) {

    public static Violation create(Priority priority,
                                   Type type,
                                   String organisationId,
                                   String transactionId,
                                   Violation.Code violationCode,
                                   Map<String, Object> bag) {
        return new Violation(priority, type, organisationId, transactionId, Optional.empty(), violationCode, bag);
    }

    public static Violation create(Priority priority,
                                   Type type,
                                   String organisationId,
                                   String transactionId,
                                   String txItemId,
                                   Violation.Code violationCode,
                                   Map<String, Object> bag) {
        return new Violation(priority, type, organisationId, transactionId, Optional.of(txItemId), violationCode, bag);
    }

    public enum Priority {
        HIGH,
        NORMAL,
    }

    public enum Type {

        WARN,

        ERROR,

        FATAL, // will fail the whole transaction line
    }

    public enum Code {
        DOCUMENT_MUST_BE_PRESENT,
        TX_ALREADY_DISPATCHED,
        ACCOUNT_CODE_CREDIT_IS_EMPTY,
        ACCOUNT_CODE_DEBIT_IS_EMPTY,
        TX_SANITY_CHECK_FAIL,
        LCY_BALANCE_MUST_BE_ZERO,
        FCY_BALANCE_MUST_ZERO,
        AMOUNT_LCY_IS_ZERO,
        AMOUNT_FCY_IS_ZERO,
        TRANSACTION_ITEMS_EMPTY,
        VAT_RATE_NOT_FOUND,
        CURRENCY_RATE_NOT_FOUND,
        COST_CENTER_NOT_FOUND,
        PROJECT_CODE_NOT_FOUND,
    }

}
