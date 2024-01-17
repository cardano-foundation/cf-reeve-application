package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.List;

public record TransactionData(List<TransactionLine> transactionLines) {
}
