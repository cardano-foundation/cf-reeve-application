package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.List;
import java.util.Set;

public record TransformationResult(TransactionLines passThroughTransactionLines,
                                   TransactionLines ignoredTransactionLines,
                                   TransactionLines filteredTransactionLines,
                                   Set<Violation> violations) {

    public static TransformationResult create(TransactionLines passThroughTransactionLines) {
        return new TransformationResult(
                passThroughTransactionLines,
                new TransactionLines(passThroughTransactionLines.organisationId(), List.of()),
                new TransactionLines(passThroughTransactionLines.organisationId(), List.of()),
                Set.of());
    }

    public static TransformationResult create(TransactionLines passThroughTransactionLines,
                                              TransactionLines ignoredTransactionLines,
                                              Set<Violation> violations) {
        return new TransformationResult(passThroughTransactionLines,
                new TransactionLines(passThroughTransactionLines.organisationId(), List.of()),
                new TransactionLines(passThroughTransactionLines.organisationId(), List.of()),
                violations);
    }

}
