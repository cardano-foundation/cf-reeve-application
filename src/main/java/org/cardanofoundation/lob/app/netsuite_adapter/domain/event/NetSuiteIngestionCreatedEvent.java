package org.cardanofoundation.lob.app.netsuite_adapter.domain.event;

import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;

public record NetSuiteIngestionCreatedEvent(Long id, TransactionDataSearchResult transactionDataSearchResult) {

}
