package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;

public record Violation(String txLineId,
                        String transactionNumber,
                        String violationCode,
                        Map<String, Object> bag) {

    public static Violation create(String txLineId, String transactionNumber, String violationCode, Map<String, Object> bag) {
        return new Violation(txLineId, transactionNumber, violationCode, bag);
    }

    public static Violation create(String txLineId, String transactionNumber, String violationCode) {
        return new Violation(txLineId, transactionNumber, violationCode, Map.of());
    }

}
