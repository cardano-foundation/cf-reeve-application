package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerStoredEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.SourceAccountingDataIngestionSuccessEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final AccountingCoreService accountingCoreService;


    @ApplicationModuleListener
    public void processFromACLLayer(SourceAccountingDataIngestionSuccessEvent event) {
        log.info("Received SourceAccountingDataIngestionSuccessEvent event.");

        accountingCoreService.store(event.transactionData());
    }

    @ApplicationModuleListener
    public void processFromBlockchainLayer(LedgerStoredEvent event) {
        log.info("Received LedgerStoredEvent event, event:{}", event);

        // read tx line ids from event
        // load entities from the database
        // set status ledger_dispatch_status to: "DISPATCHED"
        // save entities to the db
    }

}
