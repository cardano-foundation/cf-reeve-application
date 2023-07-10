package org.cardanofoundation.lob.chaindataeventconsumer.controller;


import com.bloxbean.cardano.yaci.store.metadata.storage.impl.jpa.model.TxMetadataLabelEntity;
import com.bloxbean.cardano.yaci.store.metadata.storage.impl.jpa.repository.TxMetadataLabelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/registrations")
@Log4j2
public class RegistrationsController {

    @Autowired
    private TxMetadataLabelRepository metadataLabelRepository;

    @GetMapping("/observed")
    public Flux<LedgerEventRegistration> getEventRegistrationsObserved() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, LedgerEventRegistration> registrations = new HashMap<>();
        final List<TxMetadataLabelEntity> metadataEntities = metadataLabelRepository.findAll();
        for (final TxMetadataLabelEntity metadataEntity : metadataEntities) {
            try {
                final LedgerEventRegistration ledgerEventRegistration = objectMapper.readValue(metadataEntity.getBody(), LedgerEventRegistration.class);
                if (registrations.containsKey(ledgerEventRegistration.getRegistrationId())) {
                    final List<LedgerEvent> concatenatedEvents = Streams.concat(ledgerEventRegistration.getLedgerEvents().stream(), registrations.get(ledgerEventRegistration.getRegistrationId()).getLedgerEvents().stream()).toList();
                    registrations.get(ledgerEventRegistration.getRegistrationId()).setLedgerEvents(concatenatedEvents);
                } else {
                    registrations.put(ledgerEventRegistration.getRegistrationId(), ledgerEventRegistration);
                }
            } catch (final JsonProcessingException e) {
                log.error("Cannot parse transaction metadata.", e);
            }
        }
        return Flux.fromIterable(registrations.values());
    }
}
