package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TxsApprovedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TxsDispatchApprovedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    @ApplicationModuleListener
    public void handleTxApprovedEvent(TxsApprovedEvent event) {
        log.info("Received TxsApprovedEvent event, event:{}", event.getTransactionIds());

        for (val txIds : Iterables.partition(event.getTransactionIds(), 25)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), Set.copyOf(txIds));
        }
    }

    @ApplicationModuleListener
    public void handleTxDispatchApprovedEvent(TxsDispatchApprovedEvent event) {
        log.info("Received TxsApprovedEvent event, event:{}", event.getTransactionIds());

        for (val txIds : Iterables.partition(event.getTransactionIds(), 25)) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(event.getOrganisationId(), Set.copyOf(txIds));
        }
    }

}
