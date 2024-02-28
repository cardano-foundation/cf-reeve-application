package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.data.util.Streamable;

import java.util.Set;

public interface DispatchingStrategy {

    Set<TransactionEntity> selectTransactions(String organisationId, Streamable<TransactionEntity> transactions);

}
