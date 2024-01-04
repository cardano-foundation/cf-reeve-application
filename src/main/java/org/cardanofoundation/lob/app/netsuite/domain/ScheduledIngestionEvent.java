package org.cardanofoundation.lob.app.netsuite.domain;

import org.jmolecules.event.types.DomainEvent;

public record ScheduledIngestionEvent(String initiator) implements DomainEvent {

}
