package org.cardanofoundation.lob.app.adapter.netsuite.domain;

import java.util.List;

public record TransactionDataSearchResult(boolean more, List<SearchResultTransactionItem> lines) {
}
