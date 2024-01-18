package org.cardanofoundation.lob.app.netsuite_adapter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.event.ScheduledIngestionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class NetSuiteAdapterPublicApi {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void scheduleNetsuiteIngestionEvent() {
        log.info("Handling NetSuiteStartIngestionEvent...");

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));

        log.info("Scheduled Netsuite ingestion job.");
    }

}
