package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultDispatchingStrategy implements DispatchingStrategy {

    @Value("${lob.blockchain.publisher.pullBatchSize:50}")
    private int pullBatchSize = 50;

    @Value("${lob.blockchain.publisher.minTransactions:30}")
    private int minTxCount = 35;

    @Value("${lob.blockchain.publisher.maxDelay:PT24H}")
    private Duration maxTxDelay;

    private final Clock clock;

    @PostConstruct
    public void init() {
        log.info("DefaultDispatchingStrategy initialized with pullBatchSize:{}, minTransactions:{}, maxDelay:{}",
                pullBatchSize, minTxCount, maxTxDelay);
    }

    @Override
    public Set<TransactionEntity> apply(String organisationId,
                                                     Streamable<TransactionEntity> transactions) {
        val txs = transactions.stream()
                .limit(pullBatchSize)
                .collect(Collectors.toSet());

        val now = LocalDateTime.now(clock);

        val expiredTxs = txs.stream()
             .filter(tx -> tx.getCreatedAt().plus(maxTxDelay).isAfter(now))
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
