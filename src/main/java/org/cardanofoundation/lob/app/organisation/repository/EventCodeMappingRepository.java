package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.EventCodeMapping;

import java.util.Optional;

public interface EventCodeMappingRepository {

    Optional<EventCodeMapping> getEventCodeMapping(String accountCode);

}
