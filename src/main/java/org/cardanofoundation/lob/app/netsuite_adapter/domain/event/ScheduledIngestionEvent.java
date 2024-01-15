package org.cardanofoundation.lob.app.netsuite_adapter.domain.event;

import org.jmolecules.event.types.DomainEvent;

public record ScheduledIngestionEvent(String initiator) implements DomainEvent {

}
