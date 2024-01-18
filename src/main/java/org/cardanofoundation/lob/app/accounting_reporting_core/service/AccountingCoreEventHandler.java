package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerChangeEvent;
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

        // TODO load entities from

        accountingCoreService.storeAll(event.transactionData());
    }

    @ApplicationModuleListener
    public void processFromBlockchainLayer(LedgerChangeEvent event) {
        log.info("Received LedgerChangeEvent event, event:{}", event);

        accountingCoreService.updateDispatchStatus(event.statusUpdatesMap());
    }

}
