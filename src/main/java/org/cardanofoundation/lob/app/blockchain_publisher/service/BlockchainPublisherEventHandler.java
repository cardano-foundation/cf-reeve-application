package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    private final BlockchainPublisherService blockchainPublisherService;

    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(LedgerUpdateCommand command) {
        val uploadId = command.getUploadId();
        val transactions = command.getOrganisationTransactions();

        log.info("Received LedgerUpdateCommand command..., uploadId: {}", uploadId);

        val organisationId = command.getOrganisationTransactions().organisationId();

        blockchainPublisherService.storeTransactionForDispatchLater(organisationId, transactions);
    }

}
