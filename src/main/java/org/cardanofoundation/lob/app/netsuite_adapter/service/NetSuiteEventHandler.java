package org.cardanofoundation.lob.app.netsuite_adapter.service;


import com.fasterxml.jackson.core.JsonProcessingException;
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
    public void handleScheduledIngestionEvent(ScheduledIngestionEvent event) throws JsonProcessingException {
        log.info("Handling ScheduledIngestionEvent...");

        netSuiteService.startIngestion(event.initiator(), event.filteringParameters());

        log.info("Handled ScheduledIngestionEvent.");
    }

}
