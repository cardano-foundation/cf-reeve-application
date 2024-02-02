package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.toTransactionsProjection;

public record TransactionLines(
        String organisationId,
        List<TransactionLine> entries
) {

    public static TransactionLines empty(String organisationId) {
        return new TransactionLines(organisationId, new ArrayList<>());
    }

    public Map<String, List<TransactionLine>> toTransactionsMap() {
        return toTransactionsProjection(entries);
    }

    public List<Transaction> toTransactions() {
        return Transaction.from(toTransactionsMap());
    }

}
