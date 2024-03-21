package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.val;

import java.util.LinkedHashSet;
import java.util.Set;

public record TransformationResult(OrganisationTransactions passedTransactions, // passedTransactions that passed through but still with possible violations, marked to be updated in db
                                   OrganisationTransactions ignoredTransactions, // passedTransactions that failed validation, marked to be ignored in db
                                   Set<Violation> violations) {

    public Set<Transaction> allTransactions() {
        val allTxs = new LinkedHashSet<Transaction>();
        allTxs.addAll(passedTransactions.transactions());
        allTxs.addAll(ignoredTransactions.transactions());

        return allTxs;
    }

}

