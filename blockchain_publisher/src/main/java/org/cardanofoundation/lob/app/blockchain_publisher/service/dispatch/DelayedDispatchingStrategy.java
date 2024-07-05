package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lob.dispatching.strategy", name = "type", havingValue = "DELAYED")
public class DelayedDispatchingStrategy implements DispatchingStrategy {

    @Value("${lob.blockchain.publisher.minTransactions:30}")
    private int minTxCount = 30;

    @Value("${lob.blockchain.publisher.maxDelay:PT24H}")
    private Duration maxTxDelay;

    private final Clock clock;

    @PostConstruct
    public void init() {
        log.info("DefaultDispatchingStrategy initialized with minTransactions:{}, maxDelay:{}", minTxCount, maxTxDelay);
    }

    @Override
    public Set<TransactionEntity> apply(String organisationId,
                                        Set<TransactionEntity> txs) {
        val now = LocalDateTime.now(clock);

        val prioritisedTransactions = txs.stream()
                .filter(tx -> {
                    val mustPublishDate = tx.getCreatedAt().plus(maxTxDelay);

                    return now.isAfter(mustPublishDate);
                })
                .collect(Collectors.toSet());

        if (!prioritisedTransactions.isEmpty()) {
            log.info("Found prioritised transactions for organisationId:{}, count:{}", organisationId, prioritisedTransactions.size());

            // prioritise expired transactions first since tail may not be even included in the blockchain in this run
            return new LinkedHashSet<>(Stream.concat(prioritisedTransactions.stream(), txs.stream()).toList());
        }

        log.info("Extracted {} passedTransactions for organisationId:{}", txs.size(), organisationId);

        if (txs.size() < minTxCount) {
            log.warn("Not enough passedTransactions to dispatch for organisationId:{}", organisationId);
            return Set.of();
        }

        return txs;
    }

}
