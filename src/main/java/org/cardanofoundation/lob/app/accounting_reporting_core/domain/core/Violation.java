package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.val;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
        LOB
    }

}
