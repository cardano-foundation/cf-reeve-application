package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.*;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.cardanofoundation.lob.app.support.orm.StrictApplicationModuleListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;

    private final LedgerService ledgerService;

    @ApplicationModuleListener
    public void handleERPIngestionStored(ERPIngestionStored event) {
        log.info("Received handleERPIngestionStored event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(event);

        log.info("Finished processing handleERPIngestionStored event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleERPTransactionChunk(TransactionBatchChunkEvent transactionBatchChunkEvent) {
        log.info("Received handleERPTransactionChunk event...., event, batch_id: {}, chunk_id:{}, chunkTxs:{}, chunksCount:{}", transactionBatchChunkEvent.getBatchId(), transactionBatchChunkEvent.getChunkId(), transactionBatchChunkEvent.getTransactions().size(), transactionBatchChunkEvent.getTotalChunksCount());

        erpIncomingDataProcessor.continueIngestion(
                transactionBatchChunkEvent.getOrganisationId(),
                transactionBatchChunkEvent.getChunkNo(),
                transactionBatchChunkEvent.getBatchId(),
                transactionBatchChunkEvent.getTotalTransactionsCount(),
                transactionBatchChunkEvent.getTransactions()
        );

        log.info("Finished processing handleERPTransactionChunk event...., event, batch_id: {}", transactionBatchChunkEvent.getBatchId());
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, event: {}", event.getStatusUpdates());

        ledgerService.updateTransactionsWithNewLedgerDispatchStatuses(event.getStatusUpdates());
    }

    @ApplicationModuleListener
    public void handleTxApprovedEvent(TxsApprovedEvent event) {
        log.info("Received TxsApprovedEvent event.");

        for (val partition : Partitions.partition(event.getTransactionIds(), 25)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), partition.asSet());
        }
    }

    @ApplicationModuleListener
    public void handleTxDispatchApprovedEvent(TxsDispatchApprovedEvent event) {
        log.info("Received TxsApprovedEvent event.");

        for (val partition : Partitions.partition(event.getTransactionIds(), 25)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), partition.asSet());
        }
    }

}
