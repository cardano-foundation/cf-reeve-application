package org.cardanofoundation.lob.app.accounting_core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.cardanofoundation.lob.app.notification.domain.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import static org.cardanofoundation.lob.app.notification.domain.NotificationEvent.NotificationSeverity.INFO;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void process(NetSuiteIngestionCreatedEvent event) {
      log.info("Received NetSuiteIngestionCreatedEvent event: {}", event);

        applicationEventPublisher.publishEvent(NotificationEvent.create(INFO, "NetSuiteIngestionCreatedEvent received, id: " + event.id()));
    }

}
