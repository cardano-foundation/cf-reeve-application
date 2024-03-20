package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Set;

public record TransformationResult(OrganisationTransactions passedTransactions, // passedTransactions that passed through but still with possible violations, marked to be updated in db
                                   OrganisationTransactions ignoredTransactions, // passedTransactions that failed validation, marked to be ignored in db
                                   Set<Violation> violations) {

}
