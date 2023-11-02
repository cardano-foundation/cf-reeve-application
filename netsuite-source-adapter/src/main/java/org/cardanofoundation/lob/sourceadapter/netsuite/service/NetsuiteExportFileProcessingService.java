package org.cardanofoundation.lob.sourceadapter.netsuite.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationResponse;
import org.cardanofoundation.lob.sourceadapter.netsuite.model.BulkExportLedgerEvent;
import org.cardanofoundation.lob.sourceadapter.netsuite.model.LinesLedgerEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    public LedgerEventRegistrationRequest processNetsuiteExportJson(String data) throws IOException {

        /**
         * @// TODO: 02/11/2023 Here is where we need to apply the rules or convert data and apply the rules
         */
        final List<BulkExportLedgerEvent> netsuiteExportEvents = readInLedgerEvents(data);
        final String fileHash = Hashing.blake2b256Hex(netsuiteExportEvents.toString().getBytes());

        final List<Optional<LedgerEvent>> ledgerEvents = netsuiteExportEvents.stream()
                .map(BulkExportLedgerEvent::toLedgerEvent).toList();

        final LedgerEventRegistrationRequest ledgerEventRegistrationRequest = new LedgerEventRegistrationRequest();
        ledgerEventRegistrationRequest.setRegistrationId(fileHash);
        ledgerEventRegistrationRequest.setLedgerEvents(ledgerEvents.stream().filter(Optional::isPresent).map(Optional::get).toList());
        sendRegistration(ledgerEventRegistrationRequest);

        return ledgerEventRegistrationRequest;
    }

    private Boolean sendRegistration(LedgerEventRegistrationRequest ledgerEventRegistrationRequest) {
        try {
            final Mono<LedgerEventRegistrationResponse> ledgerEventUploadMono = webClient.post()
                    .uri("/events/registrations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(ledgerEventRegistrationRequest))
                    .retrieve()
                    .bodyToMono(LedgerEventRegistrationResponse.class)

            ;
            ledgerEventUploadMono.subscribe(log::info);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    private List<BulkExportLedgerEvent> readInLedgerEvents(String data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<LinesLedgerEvent> list = Arrays.asList(mapper.readValue(data,
                    LinesLedgerEvent.class));
            return list.get(0).getLines();

        } catch (final IllegalStateException | JsonProcessingException e) {
            log.error("Given export file does not accord to the actual format.", e);

        }
        return List.of();
    }
}
