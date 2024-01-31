package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Set;

public record TransformationResult(TransactionLines passThroughTransactionLines, // entries that passed through but still with possible violations, marked to be updated in db
                                   TransactionLines ignoredTransactionLines, // entries that had issues but we do not way to save or update them
                                   Set<Violation> violations) {

}
