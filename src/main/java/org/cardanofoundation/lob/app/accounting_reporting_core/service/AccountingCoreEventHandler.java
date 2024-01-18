package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactionData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerChangeEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.SourceAccountingDataIngestionSuccessEvent;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.joining;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreEventHandler {

    private final AccountingCoreService accountingCoreService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void processIncomingIngestionAdapterEvents(SourceAccountingDataIngestionSuccessEvent event) {
        log.info("Received SourceAccountingDataIngestionSuccessEvent event.");

        // load entities from db based on ids from event

        val organisationId = event.organisationTransactionData().organisationId();

        val txLines = event.organisationTransactionData()
                .transactionLines()
                .stream()
                .toList();

        val dispatchedTxLineIds = accountingCoreService.findAllDispatchedCompletedAndFinalisedTxLineIds(organisationId, txLines.stream().map(TransactionLine::id)
                .toList());

        log.info("dispatchedTxLineIdsCount: {}", dispatchedTxLineIds.size());

        // here are conflicting ones, the ones that have already been dispatched
        val dispatchedTxLines = txLines.stream()
                .filter(txLine -> dispatchedTxLineIds.contains(txLine.id()))
                .toList();

        log.info("dispatchedTxLineCount: {}", dispatchedTxLines.size());

        if (!dispatchedTxLines.isEmpty()) {
            log.error("Failed to update = dispatchedTxLineCount: {}", dispatchedTxLines.size());

            val dispatchedTxLineIdsAsString = dispatchedTxLines
                    .stream()
                    .map(TransactionLine::id)
                    .collect(joining(","));

            applicationEventPublisher.publishEvent(NotificationEvent.create(ERROR, "Unable to update tx line ids as they have been dispatched:" + dispatchedTxLineIdsAsString));
        }

        val notDispatchedTxLines = txLines.stream()
                .filter(txLine -> !dispatchedTxLineIds.contains(txLine.id()))
                .toList();

        log.info("notDispatchedTxLinesCont: {}", notDispatchedTxLines.size());

        if (!notDispatchedTxLines.isEmpty()) {
            log.info("Storing notDispatchedTxLines: {}", notDispatchedTxLines.size());

            accountingCoreService.storeAll(new OrganisationTransactionData(organisationId, notDispatchedTxLines));
        }
    }

    @ApplicationModuleListener
    public void processFromBlockchainLayer(LedgerChangeEvent event) {
        log.info("Received LedgerChangeEvent event, event:{}", event);

        accountingCoreService.updateDispatchStatus(event.statusUpdatesMap());
    }

}
