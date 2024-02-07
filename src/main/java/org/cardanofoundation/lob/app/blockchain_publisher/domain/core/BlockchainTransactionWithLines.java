package org.cardanofoundation.lob.app.blockchain_publisher.domain.core;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;

import java.util.List;

public record BlockchainTransactionWithLines(String organisationId,
                                             List<TransactionLineEntity> submittedTransactionLines,
                                             List<TransactionLineEntity> remainingTransactionLines,
                                             byte[] serialisedTxData) {
}
