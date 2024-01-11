package org.cardanofoundation.lob.app.accounting_reporting_core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.SourceAccountingDataIngestionFailEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.SourceAccountingDataIngestionSuccessEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void process(SourceAccountingDataIngestionSuccessEvent event) {
      log.info("Received SourceAccountingDataIngestionSuccessEvent event: {}", event);

      //applicationEventPublisher.publishEvent(NotificationEvent.create(INFO, "NetSuiteIngestionCreatedEvent received, id: " + event.id()));
    }

    @ApplicationModuleListener
    public void process(SourceAccountingDataIngestionFailEvent event) {
        log.info("Received SourceAccountingDataIngestionFailEvent event: {}", event);

        //applicationEventPublisher.publishEvent(NotificationEvent.create(INFO, "NetSuiteIngestionCreatedEvent received, id: " + event.id()));
    }

}
