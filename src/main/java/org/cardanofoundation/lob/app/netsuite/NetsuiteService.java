package org.cardanofoundation.lob.app.netsuite;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.cardanofoundation.lob.app.netsuite.domain.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.netsuite.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite.util.MD5Hashing;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ResourceLoader;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@Slf4j
@AllArgsConstructor
public class NetsuiteService {

    private final IngestionRepository ingestionRepository;

    private final ResourceLoader resourceLoader;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestion> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

    @ApplicationModuleListener
    public void runIngestion(ScheduledIngestionEvent event) throws IOException {
        log.info("Running ingestion...");

        log.info("Checking if ingestion exists...");

        if (ingestionRepository.count() == 0) {
            log.info("No ingestion found. Creating one.");

            val body = resourceLoader.getResource("classpath:modules/netsuite/sandbox-data1.json").getContentAsString(UTF_8);

            val ingestionBodyChecksum = MD5Hashing.md5(body);

            val netSuiteIngestion1 = new NetSuiteIngestion();
            netSuiteIngestion1.setIngestionBody(body);
            netSuiteIngestion1.setIngestionBodyChecksum(ingestionBodyChecksum);

            ingestionRepository.saveAndFlush(netSuiteIngestion1);

            applicationEventPublisher.publishEvent(new NetSuiteIngestionCreatedEvent(netSuiteIngestion1.getId()));

            log.info("Ingestion created.");
        }

        log.info("Publishing NetSuiteIngestionCreatedEvent...");

        applicationEventPublisher.publishEvent(new NetSuiteIngestionCreatedEvent(-1L));
    }

    @Transactional
    public void scheduleNetsuiteIngestionEvent() {
        log.info("Handling NetSuiteStartIngestionEvent...");

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));

        log.info("Scheduled Netsuite ingestion job.");
    }

}
