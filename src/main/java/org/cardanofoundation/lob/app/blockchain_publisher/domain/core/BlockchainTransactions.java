package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;

import java.util.List;

public record BlockchainTransactions(String organisationId,
                                     List<TransactionEntity> submittedTransactions,
                                     List<TransactionEntity> remainingTransactions,
                                     long creationSlot,
                                     byte[] serialisedTxData) {
}
