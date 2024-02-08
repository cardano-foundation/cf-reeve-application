package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;

import java.util.List;

public record BlockchainTransactionWithLines(String organisationId,
                                             List<TransactionEntity> submittedTransactions,
                                             List<TransactionEntity> remainingTransactions,
                                             byte[] serialisedTxData) {
}
