package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.ArrayList;
import java.util.List;

public record TransactionLines(
        String organisationId,
        List<TransactionLine> entries
) {

    public static TransactionLines empty(String organisationId) {
        return new TransactionLines(organisationId, new ArrayList<>());
    }

}
