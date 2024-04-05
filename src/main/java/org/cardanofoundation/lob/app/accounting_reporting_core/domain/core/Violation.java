package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.val;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Map;
import java.util.Optional;

public record Violation(Type type,
                        Source source,
                        Optional<String> txItemId,
                        Violation.Code code,
                        String processorModule,
                        Map<String, String> bag) {

    public static Violation create(Type type,
                                   Source source,
                                   Violation.Code violationCode,
                                   String processorModule,
                                   Map<String, String> bag) {
        return new Violation(type, source, Optional.empty(), violationCode, processorModule, bag);
    }

    public static Violation create(Type type,
                                   Source source,
                                   String txItemId,
                                   Violation.Code violationCode,
                                   String processorModule,
                                   Map<String, String> bag) {
        return new Violation(type, source, Optional.of(txItemId), violationCode, processorModule, bag);
    }

    public boolean isTheSameBusinessWise(Violation violation) {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(type, violation.type);
        equalsBuilder.append(source, violation.source);
        equalsBuilder.append(txItemId, violation.txItemId);
        equalsBuilder.append(code, violation.code);
//        equalsBuilder.append(processorModule, violation.processorModule);
//        equalsBuilder.append(bag, violation.bag);

        return equalsBuilder.isEquals();
    }

    public enum Type {
        WARN,
        ERROR
    }

    public enum Source {
        ERP,
        LOB,
        INTERNAL
    }

    public enum Code {
        DOCUMENT_MUST_BE_PRESENT,
        TX_CANNOT_BE_ALTERED,
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
