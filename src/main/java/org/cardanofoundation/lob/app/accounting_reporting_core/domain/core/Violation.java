package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Priority priority,
                        Type type,
                        String organisationId,
                        String transactionId,
                        Optional<String> txItemId,
                        String violationCode,
                        Map<String, Object> bag) {

    public static Violation create(Priority priority,
                                   Type type,
                                   String organisationId,
                                   String transactionId,
                                   String violationCode,
                                   Map<String, Object> bag) {
        return new Violation(priority, type, organisationId, transactionId, Optional.empty(), violationCode, bag);
    }

    public static Violation create(Priority priority,
                                   Type type,
                                   String organisationId,
                                   String transactionId,
                                   String txItemId,
                                   String violationCode,
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

}
