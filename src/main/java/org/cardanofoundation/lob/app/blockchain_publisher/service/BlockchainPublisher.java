package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.event.TransactionsReadyToPublishEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BlockchainPublisher {

    @ApplicationModuleListener
    public void process(TransactionsReadyToPublishEvent event) {
        log.info("Received TransactionsReadyToPublishEvent event.");



        // TODO send data to the blockchain
        // or prepare data to be sent to the blockchain
        // so that user can be prompted to send it
    }

}
