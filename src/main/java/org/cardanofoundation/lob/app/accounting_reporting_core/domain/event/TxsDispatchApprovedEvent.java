package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class TxsDispatchApprovedEvent {

    private String organisationId;
    private Set<String> transactionIds;

    public static TxsDispatchApprovedEvent of(String organisationId, String txId) {
        return new TxsDispatchApprovedEvent(organisationId, Set.of(txId));
    }

}
