package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.IngestionPipelineProcessor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final IngestionPipelineProcessor ingestionPipelineProcessor;

    private final NotificationGateway notificationGateway;

    private final LedgerService ledgerService;

    @ApplicationModuleListener
    public void handleERPIngestionEvent(ERPIngestionEvent event) {
        log.info("Received handleERPIngestionEvent event....");

        val transformationResult = ingestionPipelineProcessor.runPipeline(event.transactionLines());
        notificationGateway.sendViolationNotifications(transformationResult.violations());

        log.info("Finished processing...");
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, eventCounts:{}", event.statusUpdates().size());

        ledgerService.updateTransactionLines(event.statusUpdates());
    }

}
