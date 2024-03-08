package org.cardanofoundation.lob.app.netsuite_adapter.domain.core;

import java.util.List;

public record TransactionDataSearchResult(List<TxLine> lines,
                                          boolean more) {
}
