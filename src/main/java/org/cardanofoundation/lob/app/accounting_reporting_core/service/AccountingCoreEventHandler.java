package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.IngestionPipelineProcessor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final IngestionPipelineProcessor ingestionPipelineProcessor;

    private final LedgerService ledgerService;

    @ApplicationModuleListener
    public void handleERPIngestionEvent(ERPIngestionEvent event) {
        log.info("Received handleERPIngestionEvent event....");

        val organisationId = event.transactionLines().organisationId();

        ingestionPipelineProcessor.run(
                event.transactionLines(),
                TransactionLines.empty(organisationId),
                Set.of()
        );

        log.info("Finished processing...");
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, eventCounts:{}", event.statusUpdates().size());

        ledgerService.updateTransactionLines(event.statusUpdates());
    }

}
