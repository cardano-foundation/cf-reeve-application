package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerStoredEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.PublishToTheLedgerEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    public final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void process(PublishToTheLedgerEvent event) {
        log.info("Received IngestionStoredEvent event.");

        val blockchainDispatchedTxLineIds = event.txData().transactionLines()
                .stream()
                .map(TransactionLine::id)
                .toList();

        // TODO send data to the blockchain
        // or prepare data to be sent to the blockchain
        // so that user can be prompted to send it

        applicationEventPublisher.publishEvent(new LedgerStoredEvent(blockchainDispatchedTxLineIds));
    }

}
