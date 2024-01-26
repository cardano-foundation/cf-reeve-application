package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final AccountingCoreService accountingCoreService;

    @ApplicationModuleListener
    // TODO move this to the service layer
    public void handleERPIngestionEvent(ERPIngestionEvent event) {
        log.info("Received handleERPIngestionEvent event....");

        accountingCoreService.runIncomingIngestionPipeline(event.transactionLines());
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, eventCounts:{}", event.statusUpdates().size());

        accountingCoreService.syncStateFromLedger(event.statusUpdates());
    }

}
