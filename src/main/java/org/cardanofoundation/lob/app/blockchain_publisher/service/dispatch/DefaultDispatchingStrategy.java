package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultDispatchingStrategy implements DispatchingStrategy {

    @Value("${lob.blockchain.publisher.pullBatchSize:50}")
    private int pullBatchSize = 50;

    @Value("${lob.blockchain.publisher.minTransactions:30}")
    private int minTxCount = 35;

    @Value("${lob.blockchain.publisher.maxDelay:PT1H}")
    private Duration maxTxDelay;

    @Override
    public Set<TransactionEntity> filter(String organisationId,
                                         Streamable<TransactionEntity> transactions) {
        val txs = transactions.stream()
                .limit(pullBatchSize)
                .collect(Collectors.toSet());

        val expiredTxs = txs.stream()
             .filter(tx -> tx.getCreatedAt().isAfter(tx.getCreatedAt().plus(maxTxDelay)))
             .collect(Collectors.toSet());

        if (!expiredTxs.isEmpty()) {
            return expiredTxs;
        }

        if (txs.size() < minTxCount) {
            log.warn("Not enough organisationTransactions to dispatch for organisationId:{}", organisationId);
            return Set.of();
        }

        return txs;
    }

}
