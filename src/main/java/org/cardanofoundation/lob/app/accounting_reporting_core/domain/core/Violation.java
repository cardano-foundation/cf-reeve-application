package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Type type,
                        String organisationId,
                        String transactionId,
                        Optional<String> txItemId,
                        Violation.Code code,
                        String processorModule,
                        Map<String, Object> bag) {

    public static Violation create(Type type,
                                   String organisationId,
                                   String transactionId,
                                   Violation.Code violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(type, organisationId, transactionId, Optional.empty(), violationCode, processorModule, bag);
    }

    public static Violation create(Type type,
                                   String organisationId,
                                   String transactionId,
                                   String txItemId,
                                   Violation.Code violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(type, organisationId, transactionId, Optional.of(txItemId), violationCode, processorModule, bag);
    }

    public enum Type {
        WARN,
        ERROR
    }

    public enum Code {
        DOCUMENT_MUST_BE_PRESENT,
        TX_ALREADY_DISPATCHED,
        ACCOUNT_CODE_CREDIT_IS_EMPTY,
        ACCOUNT_CODE_DEBIT_IS_EMPTY,
        TX_SANITY_CHECK_FAIL,
        LCY_BALANCE_MUST_BE_ZERO,
        FCY_BALANCE_MUST_BE_ZERO,
        AMOUNT_LCY_IS_ZERO,
        AMOUNT_FCY_IS_ZERO,
        TRANSACTION_ITEMS_EMPTY,
        VAT_RATE_NOT_FOUND,
        CORE_CURRENCY_NOT_FOUND,
        CURRENCY_NOT_FOUND,
        COST_CENTER_NOT_FOUND,
        PROJECT_CODE_NOT_FOUND,
        CHART_OF_ACCOUNT_NOT_FOUND,
        ORGANISATION_NOT_FOUND,
    }

}
