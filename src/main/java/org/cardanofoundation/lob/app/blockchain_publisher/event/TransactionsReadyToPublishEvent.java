package org.cardanofoundation.lob.app.blockchain_publisher.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;

public record TransactionsReadyToPublishEvent(TransactionData transactionData) {
}
