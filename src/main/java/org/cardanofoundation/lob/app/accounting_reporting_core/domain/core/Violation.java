package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Priority priority,
                        Type type,
                        Optional<String> txLineId,
                        String transactionNumber,
                        String violationCode,
                        Map<String, Object> bag) {

    public static Violation create(Priority priority,
                                   Type type,
                                   String txLineId,
                                   String transactionNumber,
                                   String violationCode,
                                   Map<String, Object> bag) {
        return new Violation(priority, type, Optional.of(txLineId), transactionNumber, violationCode, bag);
    }

    public static Violation create(Priority priority,
                                   Type type,
                                   String txLineId,
                                   String transactionNumber,
                                   String violationCode) {
        return new Violation(priority, type, Optional.of(txLineId), transactionNumber, violationCode, Map.of());
    }

    public static Violation create(Priority priority,
                                   Type type,
                                   String transactionNumber,
                                   String violationCode,
                                   Map<String, Object> bag) {
        return new Violation(priority, type, Optional.empty(), transactionNumber, violationCode, bag);
    }

    public static Violation create(Priority priority,
                                   Type type,
                                   String transactionNumber,
                                   String violationCode) {
        return new Violation(priority, type, Optional.empty(), transactionNumber, violationCode, Map.of());
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

}
