package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    private final BlockchainPublisherService blockchainPublisherService;

    private final OrganisationPublicApi organisationPublicApi;

    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(LedgerUpdateCommand command) {
        val uploadId = command.getUploadId();
        val transactionLines = command.getOrganisationTransactions();

        log.info("Received LedgerUpdateCommand command..., uploadId: {}", uploadId);

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.id();
            log.info("Dispatching transactions for organisation:{}", organisationId);

            blockchainPublisherService.dispatchTransactionsToBlockchains(organisationId, transactionLines);
        }
    }

}
