package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;
import java.util.Optional;

public record Violation(Type type,
                        Source source,
                        Optional<String> txItemId,
                        ViolationCode code,
                        String processorModule,
                        Map<String, Object> bag) {

    public static Violation create(Type type,
                                   Source source,
                                   ViolationCode violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(type, source, Optional.empty(), violationCode, processorModule, bag);
    }

    public static Violation create(Type type,
                                   Source source,
                                   String txItemId,
                                   ViolationCode violationCode,
                                   String processorModule,
                                   Map<String, Object> bag) {
        return new Violation(type, source, Optional.of(txItemId), violationCode, processorModule, bag);
    }

    public enum Type {
        WARN,
        ERROR
    }

}
