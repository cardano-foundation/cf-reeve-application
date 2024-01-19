package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.jmolecules.event.types.DomainEvent;

public record ScheduledIngestionEvent(String initiator) implements DomainEvent {

}
