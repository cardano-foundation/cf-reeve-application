package org.cardanofoundation.lob.app.netsuite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite.domain.NetSuiteIngestionCreatedEvent;
import org.cardanofoundation.lob.app.netsuite.domain.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.netsuite.domain.TransactionData;
import org.cardanofoundation.lob.app.netsuite.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite.util.MoreCompress;
import org.cardanofoundation.lob.app.notification.domain.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.Optional;

import static org.cardanofoundation.lob.app.notification.domain.NotificationEvent.NotificationSeverity.ERROR;
import static org.cardanofoundation.lob.app.notification.domain.NotificationEvent.NotificationSeverity.WARN;

@Service
@Slf4j
@AllArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NetSuiteAPI netSuiteAPI;

    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<NetSuiteIngestion> findIngestionById(String id) {
        return ingestionRepository.findById(id);
    }

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines() {
        return netSuiteAPI.retrieveLatestNetsuiteTransactionLines();
    }

    @ApplicationModuleListener
    public void runIngestion(ScheduledIngestionEvent event) throws JsonProcessingException {
        log.info("Running ingestion...");

        log.info("Checking if ingestion exists...");

        if (ingestionRepository.count() == 0) {
            log.info("No ingestion found. Creating one.");

            var netSuiteJsonE = retrieveLatestNetsuiteTransactionLines();

            if (netSuiteJsonE.isEmpty()) {
                log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

                applicationEventPublisher.publishEvent(NotificationEvent.create(
                        ERROR,
                        "Error retrieving data from NetSuite API",
                        netSuiteJsonE.getLeft())
                );

                return;
            }

            val bodyM = netSuiteJsonE.get();

            if (bodyM.isEmpty()) {
                log.warn("No data to read from NetSuite API..., bailing out!");

                applicationEventPublisher.publishEvent(NotificationEvent.create(
                        WARN,
                        "No data to read from NetSuite API"));
                return;
            }

            val netsuiteTransactionLines = bodyM.get();

            log.info(netsuiteTransactionLines);

            //val body = resourceLoader.getResource("classpath:modules/netsuite/sandbox-data1.json").getContentAsString(UTF_8);

            val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLines);

            val netSuiteIngestion = new NetSuiteIngestion();

            val compressedBody = MoreCompress.compress(netsuiteTransactionLines);

            log.info("Before compression: {}, compressed: {}", netsuiteTransactionLines.length(), compressedBody.length());

            netSuiteIngestion.setIngestionBody(compressedBody);
            netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

            ingestionRepository.saveAndFlush(netSuiteIngestion);

            val transactionData = objectMapper.readValue(netsuiteTransactionLines, TransactionData.class);

            applicationEventPublisher.publishEvent(new NetSuiteIngestionCreatedEvent(netSuiteIngestion.getId(), transactionData));

            log.info("Ingestion created.");
        }
    }

    @Transactional
    public void scheduleNetsuiteIngestionEvent() {
        log.info("Handling NetSuiteStartIngestionEvent...");

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));

        log.info("Scheduled Netsuite ingestion job.");
    }

}
