package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.*;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;

    private final LedgerService ledgerService;

    private final TransactionBatchService transactionBatchService;

    @Value("${lob.blockchain_publisher.send.batch.size:25}")
    private int sendBatchSize = 25;

    @ApplicationModuleListener
    public void handleERPIngestionStored(ERPIngestionStored event) {
        log.info("Received handleERPIngestionStored event, event: {}", event);

        erpIncomingDataProcessor.initiateIngestion(event);

        log.info("Finished processing handleERPIngestionStored event, event: {}", event);
    }

    @ApplicationModuleListener
    public void handleERPTransactionChunk(TransactionBatchChunkEvent transactionBatchChunkEvent) {
        log.info("Received handleERPTransactionChunk event...., event, batch_id: {}, chunk_size:{}", transactionBatchChunkEvent.getBatchId(), transactionBatchChunkEvent.getTransactions().size());

        erpIncomingDataProcessor.continueIngestion(
                transactionBatchChunkEvent.getOrganisationId(),
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

        for (val partition : Partitions.partition(event.getTransactionIds(), sendBatchSize)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), partition.asSet());
        }
    }

    @ApplicationModuleListener
    public void handleTxDispatchApprovedEvent(TxsDispatchApprovedEvent event) {
        log.info("Received TxsApprovedEvent event.");

        for (val partition : Partitions.partition(event.getTransactionIds(), sendBatchSize)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), partition.asSet());
        }
    }

    @ApplicationModuleListener
    public void handleBusinessRulesApplied(BusinessRulesAppliedEvent event) {
        log.info("Received BusinessRulesAppliedEvent event, event: {}", event.getOrganisationId());

        transactionBatchService.updateTransactionBatchStatusAndStats(
                event.getBatchId(),
                event.getTotalTransactionsCount()
        );
    }

}
