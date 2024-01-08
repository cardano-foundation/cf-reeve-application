package org.cardanofoundation.lob.app.netsuite.domain;

import java.util.List;

public record TransactionData(boolean more, List<TransactionLine> lines) {
}
