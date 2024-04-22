package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    public void handleERPIngestionStored(ERPIngestionStored event) {
        log.info("Received handleERPIngestionStored event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(event);

        log.info("Finished processing handleERPIngestionStored event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleERPTransactionChunk(TransactionBatchChunkEvent transactionBatchChunkEvent) {
        log.info("Received handleERPTransactionChunk event...., event, batch_id: {}, chunk_size:{}", transactionBatchChunkEvent.getBatchId(), transactionBatchChunkEvent.getTransactions().size());

        val detachedDbTxs = transactionConverter.convertToDbDetached(transactionBatchChunkEvent.getTransactions());

        erpIncomingDataProcessor.continueIngestion(
                transactionBatchChunkEvent.getOrganisationId(),
                transactionBatchChunkEvent.getBatchId(),
                transactionBatchChunkEvent.getTotalTransactionsCount(),
                detachedDbTxs,
                ProcessorFlags.builder()
                        .reprocess(false)
                        .build()
        );

        log.info("Finished processing handleERPTransactionChunk event...., event, batch_id: {}", transactionBatchChunkEvent.getBatchId());
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, event: {}", event.getStatusUpdates());

        val txStatusUpdatesMap = event.statusUpdatesMap();

        ledgerService.updateTransactionsWithNewLedgerDispatchStatuses(txStatusUpdatesMap);
        transactionBatchService.updateBatchesPerTransactions(txStatusUpdatesMap);
    }

}
