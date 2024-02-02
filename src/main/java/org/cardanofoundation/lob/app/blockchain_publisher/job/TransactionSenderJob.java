package org.cardanofoundation.lob.app.blockchain_publisher.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionSenderJob {

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT10S")
    public void execute() {
        log.info("Polling for to send transactions to the blockchain...");

    }

}
