package org.cardanofoundation.lob.app.netsuite_adapter.domain.core;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;

import java.util.Map;
import java.util.Set;

public record TransactionsWithViolations(String organisationId,
                                         Set<Transaction> transactions,
                                         Set<Violation> violations) {


}
