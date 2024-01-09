package org.cardanofoundation.lob.app.accounting_acl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_acl.domain.IngestedTransactionDataEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingAntiCorruptionLayerService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void process(IngestedTransactionDataEvent event) {
      log.info("Received IngestedTransactionDataEvent event: {}", event);

      //applicationEventPublisher.publishEvent(NotificationEvent.create(INFO, "NetSuiteIngestionCreatedEvent received, id: " + event.id()));
    }

}
