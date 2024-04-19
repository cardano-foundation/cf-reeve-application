package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Violation;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TransformationResult(OrganisationTransactions passedTransactions,// passedTransactions that passed through but still with possible violations, marked to be updated in db
                                   OrganisationTransactions ignoredTransactions// passedTransactions that failed validation, marked to be ignored in db
) {

    public Set<Violation> violations() {
        return getAllViolations();
    }

    public Set<Violation> getAllViolations() {
        return Stream.concat(passedTransactions.transactions().stream(), ignoredTransactions.transactions().stream())
                .flatMap(transaction -> transaction.getViolations().stream())
                .collect(Collectors.toSet());
    }

}
