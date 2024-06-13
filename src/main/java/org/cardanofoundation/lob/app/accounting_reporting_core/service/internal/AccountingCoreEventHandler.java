package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.BatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;
    private final TransactionConverter transactionConverter;
    private final LedgerService ledgerService;
    private final TransactionBatchService transactionBatchService;

    @ApplicationModuleListener
    public void handleBatchFailed(BatchFailedEvent event) {
        log.info("Received batchFailedEvent event, event: {}", event);

        val error = event.getError();

        transactionBatchService.failTransactionBatch(event.getBatchId(), error);

        log.info("Finished processing batchFailedEvent event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleERPIngestionStored(ERPIngestionStored event) {
        log.info("Received handleERPIngestionStored event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(event);

        log.info("Finished processing handleERPIngestionStored event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleERPTransactionChunk(TransactionBatchChunkEvent transactionBatchChunkEvent) {
        String batchId = transactionBatchChunkEvent.getBatchId();

        log.info("Received handleERPTransactionChunk event...., event, batch_id: {}, chunk_size:{}", batchId, transactionBatchChunkEvent.getTransactions().size());

        val txs = transactionBatchChunkEvent.getTransactions();
        val detachedDbTxs = transactionConverter.convertToDbDetached(txs);

        erpIncomingDataProcessor.continueIngestion(
                transactionBatchChunkEvent.getOrganisationId(),
                batchId,
                transactionBatchChunkEvent.getTotalTransactionsCount(),
                detachedDbTxs,
                ProcessorFlags.builder()
                        .reprocess(false)
                        .build()
        );

        log.info("Finished processing handleERPTransactionChunk event...., event, batch_id: {}", batchId);
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, event: {}", event.getStatusUpdates());

        val txStatusUpdatesMap = event.statusUpdatesMap();

        ledgerService.updateTransactionsWithNewStatuses(txStatusUpdatesMap);
        transactionBatchService.updateBatchesPerTransactions(txStatusUpdatesMap);
    }

}
