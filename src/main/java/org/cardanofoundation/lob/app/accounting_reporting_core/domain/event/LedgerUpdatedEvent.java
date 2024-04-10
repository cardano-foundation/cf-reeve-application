package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@DomainEvent
public final class LedgerUpdatedEvent {

    private String organisationId;
    private Set<TxStatusUpdate> statusUpdates;

}
