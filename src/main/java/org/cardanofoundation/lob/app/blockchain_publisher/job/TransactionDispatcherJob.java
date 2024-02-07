package org.cardanofoundation.lob.app.blockchain_publisher.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainTransactionsDispatcher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionDispatcherJob {

    private final BlockchainTransactionsDispatcher blockchainTransactionsDispatcher;

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT2M")
    public void execute() {
        log.info("Pooling for blockchain transactions to be send to the blockchain...");

        blockchainTransactionsDispatcher.dispatchTransactions();

        log.info("Pooling for blockchain transactions to be send to the blockchain...done");
    }

}
