package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

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

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;

    private final LedgerService ledgerService;

    @ApplicationModuleListener
    public void handleERPIngestionEvent(ERPIngestionEvent event) {
        //log.info("Received handleERPIngestionEvent event...., event:{}", event);

        erpIncomingDataProcessor.processIncomingERPEvent(event.getOrganisationTransactions());

        //log.info("Finished processing...");
    }

    @ApplicationModuleListener
    public void handleLedgerUpdatedEvent(LedgerUpdatedEvent event) {
        log.info("Received LedgerUpdatedEvent event, event:{}", event.getStatusUpdates());

        ledgerService.updateTransactionsWithNewLedgerDispatchStatuses(event.getStatusUpdates());
    }

}
