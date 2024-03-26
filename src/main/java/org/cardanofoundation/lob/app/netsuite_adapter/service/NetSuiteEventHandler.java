package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchCreatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetSuiteEventHandler {

    private final NetSuiteService netSuiteService;

    @ApplicationModuleListener
    public void handleScheduledIngestionEvent(ScheduledIngestionEvent event) {
        log.info("Handling ScheduledIngestionEvent...");

        netSuiteService.startERPExtraction(
                event.getInitiator(),
                event.getUserExtractionParameters()
        );

        log.info("Handled ScheduledIngestionEvent.");
    }

    @ApplicationModuleListener
    public void handleTransactionBatchCreatedEvent(TransactionBatchCreatedEvent transactionBatchCreatedEvent) {
        log.info("Handling TransactionBatchCreatedEvent...");

        netSuiteService.continueERPExtraction(
                transactionBatchCreatedEvent.getBatchId(),
                transactionBatchCreatedEvent.getInstanceId(),
                transactionBatchCreatedEvent.getUserExtractionParameters(),
                transactionBatchCreatedEvent.getSystemExtractionParameters()
        );

        log.info("Handled ScheduledIngestionEvent.");
    }

}
