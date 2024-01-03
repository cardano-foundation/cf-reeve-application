package org.cardanofoundation.lob.app.core;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CoreService {

    @ApplicationModuleListener
    public void process(NetSuiteIngestionCreatedEvent event) {
      log.info("Received NetSuiteIngestionCreatedEvent event: {}", event);
    }

}
