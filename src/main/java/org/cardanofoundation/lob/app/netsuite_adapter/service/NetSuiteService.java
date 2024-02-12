package org.cardanofoundation.lob.app.netsuite_adapter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionEvent;
import org.cardanofoundation.lob.app.netsuite_adapter.client.NetSuiteAPI;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.cardanofoundation.lob.app.netsuite_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MD5Hashing;
import org.cardanofoundation.lob.app.netsuite_adapter.util.MoreCompress;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
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
    public Optional<NetSuiteIngestion> findIngestionById(UUID id) {
        return ingestionRepository.findById(id);
    }

    public Either<Problem, Optional<String>> retrieveLatestNetsuiteTransactionLines() {
        return netSuiteAPI.retrieveLatestNetsuiteTransactionLines();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void startIngestion(String initiator, FilteringParameters filteringParameters) throws JsonProcessingException {
        log.info("Running ingestion...");

        log.info("Checking if ingestion exists...");

        log.info("No ingestion found. Creating one.");

        val netSuiteJsonE = retrieveLatestNetsuiteTransactionLines();

        if (netSuiteJsonE.isEmpty()) {
            log.error("Error retrieving data from NetSuite API: {}", netSuiteJsonE.getLeft().getDetail());

            applicationEventPublisher.publishEvent(NotificationEvent.create(
                    ERROR,
                    "NETSUITE_API_ERROR",
                    "Error retrieving data from NetSuite API",
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
                    "NETSUITE_API_NO_DATA_ERROR",
                    "No data to read from NetSuite API",
                    "No data to read from NetSuite API"));
            return;
        }

        val netsuiteTransactionLinesJson = bodyM.get();
        //log.info("Retrieved data from NetSuite API: {}", netsuiteTransactionLinesJson);

        val ingestionBodyChecksum = MD5Hashing.md5(netsuiteTransactionLinesJson);

        val netSuiteIngestion = new NetSuiteIngestion();

        val compressedBody = MoreCompress.compress(netsuiteTransactionLinesJson);

        log.info("Before compression: {}, compressed: {}", netsuiteTransactionLinesJson.length(), compressedBody.length());

        //netSuiteIngestion.setIngestionBody(compressedBody);
        netSuiteIngestion.setIngestionBody(netsuiteTransactionLinesJson);
        netSuiteIngestion.setIngestionBodyChecksum(ingestionBodyChecksum);

        ingestionRepository.saveAndFlush(netSuiteIngestion);

        val transactionDataSearchResult = objectMapper.readValue(netsuiteTransactionLinesJson, TransactionDataSearchResult.class);

        log.info("transactionDataSearchResult count:{}", transactionDataSearchResult.lines().size());

        val validatedTransactionLineItems = transactionDataSearchResult
                .lines()
                .stream()
                .filter(searchResultTransactionItem -> {
                    val issues = validator.validate(searchResultTransactionItem);
                    val isValid = issues.isEmpty();

                    if (!isValid) {
                        log.warn("Invalid transaction line item: {}", searchResultTransactionItem);

                        log.warn("Validation issues: {}", issues.iterator().next());
                    }

                    return isValid;
                })
                .toList();

        log.info("validatedTransactionLineItems count:{}", validatedTransactionLineItems.size());

        val coreTransactionLines = validatedTransactionLineItems.stream()
                .map(item -> transactionLineConverter.convert(netSuiteIngestion.getId(), item))
                .filter(line -> filteringParameters.getFrom().isEmpty() || filteringParameters.getFrom().map(date -> line.getEntryDate().isAfter(date)).orElse(false))
                .filter(line -> filteringParameters.getTransactionTypes().isEmpty() || filteringParameters.getTransactionTypes().contains(line.getTransactionType()))
                .filter(line -> filteringParameters.getTo().isEmpty() || filteringParameters.getTo().map(date -> line.getEntryDate().isBefore(date)).orElse(false))
                .filter(line -> filteringParameters.getOrganisationIds().isEmpty() || filteringParameters.getOrganisationIds().contains(line.getOrganisationId()))
                .filter(line -> filteringParameters.getProjectCodes().isEmpty() || line.getInternalProjectCode().map(projectCode -> filteringParameters.getProjectCodes().contains(projectCode)).orElse(false))
                .toList();

        log.info("CoreTransactionLines count: {}", coreTransactionLines.size());

        val txLinesPerOrganisationId = coreTransactionLines.stream()
                .collect(groupingBy(TransactionLine::getOrganisationId));

        for (val entry : txLinesPerOrganisationId.entrySet()) {
            val organisationId = entry.getKey();
            val txLines = entry.getValue();

            log.info("Publishing ERPIngestionEvent event, organisationId: {}, txLines count: {}", organisationId, txLines.size());

            applicationEventPublisher.publishEvent(new ERPIngestionEvent(
                    initiator,
                    filteringParameters,
                    new TransactionLines(organisationId, txLines)));
        }

        log.info("NetSuite Ingestion completed.");
    }

}
