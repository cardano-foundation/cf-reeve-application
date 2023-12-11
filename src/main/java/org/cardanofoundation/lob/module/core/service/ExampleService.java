package org.cardanofoundation.lob.module.core.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.module.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExampleService {

    @ApplicationModuleListener
    public void process(NetSuiteIngestionCreatedEvent event) {
      log.info("Received NetSuiteIngestionCreatedEvent event: {}", event);
    }

}
