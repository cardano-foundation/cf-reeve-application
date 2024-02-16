package org.cardanofoundation.lob.app.netsuite_adapter.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
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

        netSuiteService.startERPExtraction(event.getInitiator(), event.getFilteringParameters());

        log.info("Handled ScheduledIngestionEvent.");
    }

}
