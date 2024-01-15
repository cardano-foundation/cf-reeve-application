package org.cardanofoundation.lob.app.netsuite_adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.SourceAccountingDataIngestionSuccessEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.service.TransactionLineConverter;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.Optional;

import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.WARN;

@Service
@Slf4j
@AllArgsConstructor
public class NetSuiteService {

    private final IngestionRepository ingestionRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NetSuiteAPI netSuiteAPI;

    @Qualifier("netSuiteJsonMapper")
    private final ObjectMapper objectMapper;

    private final Validator validator;

    private final TransactionLineConverter transactionLineConverter;

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

        log.info("No ingestion found. Creating one.");

        val netSuiteJsonE = retrieveLatestNetsuiteTransactionLines();

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

        val netsuiteTransactionLinesJson = bodyM.get();
        //log.info("Retrieved data from NetSuite API: {}", netsuiteTransactionLinesJson);

        val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLinesJson);

        val netSuiteIngestion = new NetSuiteIngestion();

        val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);

        assert compressedBody != null;

        log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

        netSuiteIngestion.setIngestionBody(compressedBody);
        netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

        ingestionRepository.saveAndFlush(netSuiteIngestion);

        val transactionDataSearchResult = objectMapper.readValue(netsuiteTransactionLinesJson, TransactionDataSearchResult.class);

        

        val validatedTransactionLineItems = transactionDataSearchResult
                .lines()
                .stream()
                .filter(searchResultTransactionItem -> validator.validate(searchResultTransactionItem).isEmpty())
                .toList();

        val coreTransactionLines = validatedTransactionLineItems.stream()
                .map(transactionLineConverter::convert)
                .toList();

        applicationEventPublisher.publishEvent(new SourceAccountingDataIngestionSuccessEvent(new TransactionData(coreTransactionLines)));

        log.info("Ingestion created.");
    }

    @Transactional
    public void scheduleNetsuiteIngestionEvent() {
        log.info("Handling NetSuiteStartIngestionEvent...");

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));

        log.info("Scheduled Netsuite ingestion job.");
    }

}
