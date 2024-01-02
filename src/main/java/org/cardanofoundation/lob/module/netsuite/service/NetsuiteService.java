package org.cardanofoundation.lob.module.netsuite.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.module.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.cardanofoundation.lob.module.netsuite.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.module.netsuite.repository.IngestionRepository;
import org.cardanofoundation.lob.module.netsuite.util.MD5Hashing;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ResourceLoader;
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

    @PostConstruct
    public void init() {
        log.info("NetsuiteProcessor init.");
    }

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestion> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

    @Transactional
    public void runIngestion() throws IOException {
        log.info("Running ingestion...");

        log.info("Checking if ingestion exists...");

        if (ingestionRepository.count() == 0) {
            log.info("No ingestion found. Creating one.");

            val body = resourceLoader.getResource("classpath:modules/netsuite/sandbox-data1.json").getContentAsString(UTF_8);

            val ingestionBodyChecksum = MD5Hashing.md5(body);

            val netSuiteIngestion1 = NetSuiteIngestion.builder()
                    .id(ingestionBodyChecksum)
                    .version(1L) // TODO jpa auditing (envers)
                    .ingestionBody(body)
                    .ingestionBodyChecksum(ingestionBodyChecksum)
                    .build();

            ingestionRepository.saveAndFlush(netSuiteIngestion1);

            String id = netSuiteIngestion1.getId();
            applicationEventPublisher.publishEvent(new NetSuiteIngestionCreatedEvent(id));

            log.info("Ingestion created.");
        }

        applicationEventPublisher.publishEvent(new NetSuiteIngestionCreatedEvent("FAKE_ID"));
    }

}
