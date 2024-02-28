package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.EventCodeMapping;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StaticEventCodeMappingRepository implements EventCodeMappingRepository {

    @Override
    public Optional<EventCodeMapping> getEventCodeMapping(String accountCode) {
        return Optional.of(new EventCodeMapping(accountCode, STR."R:\{accountCode}"));
    }

}
