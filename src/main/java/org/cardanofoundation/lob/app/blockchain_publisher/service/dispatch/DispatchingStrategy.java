package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.data.util.Streamable;

import java.util.Set;
import java.util.function.BiFunction;

public interface DispatchingStrategy extends BiFunction<String, Set<TransactionEntity>, Set<TransactionEntity>> {

}
