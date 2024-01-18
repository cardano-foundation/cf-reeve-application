package org.cardanofoundation.lob.app.blockchain_publisher.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionLineScanningJob {

    @Scheduled(fixedDelay = 60_000L) // TODO job should be configurable
    public void checkTransactionStatuses() {
        log.info("Polling for to check for transaction statuses...");

        // gets transaction lines from the database which have not been finalished yet
        // checks if transaction is finalised.
        // sends event to Accounting Core with update to the transaction lines which changed their status
    }

}
