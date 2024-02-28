package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.EventCodeMapping;
import org.cardanofoundation.lob.app.organisation.repository.EventCodeMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventCodeMappingService {

    private final EventCodeMappingRepository eventCodeMappingRepository;

    public Optional<EventCodeMapping> getEventCodeMapping(String accountCode) {
        return eventCodeMappingRepository.getEventCodeMapping(accountCode);
    }

}
