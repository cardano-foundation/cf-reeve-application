package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.sourceadapter.netsuite.model.BulkExportLedgerEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
@Log4j2
public class NetsuiteExportFileProcessingService {

    @Value("${lob.netsuite.inboundFiles.fileFormat.separator}")
    private Character separatorChar;

    @Value("${lob.netsuite.inboundFiles.fileFormat.quoteChar}")
    private Character quoteChar;

    @Value("${lob.netsuite.inboundFiles.fileFormat.skipLines}")
    private Integer skipLines;

    @Value("${lob.netsuite.sourceApi.baseUrl}")
    private String sourceApiBaseUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.create(sourceApiBaseUrl);
    }

    private List<BulkExportLedgerEvent> readInLedgerEvents(final Path exportFile) {
        try {
            final CsvToBeanBuilder<BulkExportLedgerEvent> csvToBeanBuilder = new CsvToBeanBuilder<BulkExportLedgerEvent>(new FileReader(exportFile.toFile()))
                    .withSeparator(separatorChar).withSkipLines(skipLines).withType(BulkExportLedgerEvent.class);
            if (quoteChar != null) {
                csvToBeanBuilder.withQuoteChar(quoteChar);
            }

            return csvToBeanBuilder.build().parse();
        } catch (final FileNotFoundException e) {
            log.error("Netsuite export file to read does not exist.", e);
            return List.of();
        } catch (final IllegalStateException e) {
            log.error("Given export file does not accord to the actual format.", e);
            return List.of();
        }
    }

    public void processNetsuiteExportFiles(final Path exportFile) {
        final List<BulkExportLedgerEvent> netsuiteExportEvents = readInLedgerEvents(exportFile);
        final List<Optional<LedgerEvent>> ledgerEvents = netsuiteExportEvents.stream()
                .filter(bulkExportLedgerEvent -> bulkExportLedgerEvent.getAccountLineName() != null &&
                        bulkExportLedgerEvent.getAccountName() != null &&
                        !bulkExportLedgerEvent.getAccountLineName().trim().equalsIgnoreCase(bulkExportLedgerEvent.getAccountName().trim()) &&
                        bulkExportLedgerEvent.getAmountForeignCurrency() != null &&
                        bulkExportLedgerEvent.getAmountForeignCurrency() > 0.0 &&
                        bulkExportLedgerEvent.getEntityId() != null &&
                        StringUtils.isNumeric(bulkExportLedgerEvent.getEntityId()))
                .map(BulkExportLedgerEvent::toLedgerEvent).toList();

        log.info(ledgerEvents);

        final LedgerEventRegistrationRequest ledgerEventRegistrationRequest = new LedgerEventRegistrationRequest();
        ledgerEventRegistrationRequest.setRegistrationId("123");
        ledgerEventRegistrationRequest.setLedgerEvents(ledgerEvents.stream().filter(Optional::isPresent).map(Optional::get).toList());
        final Mono<LedgerEventRegistrationRequest> ledgerEventUploadMono = webClient.post()
                .uri("/events/registrations")
                .body(BodyInserters.fromValue(ledgerEventRegistrationRequest))
                .retrieve()
                .bodyToMono(LedgerEventRegistrationRequest.class);

        ledgerEventUploadMono.subscribe(log::info);
    }
}
