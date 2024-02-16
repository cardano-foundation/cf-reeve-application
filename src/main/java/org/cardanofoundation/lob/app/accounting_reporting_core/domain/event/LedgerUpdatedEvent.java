package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;

import java.util.Set;

/**
 * Event used after data has been sent or updated on the blockchain
 *
 * @param statusUpdates
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class LedgerUpdatedEvent {

    private String organisationId;
    private Set<TxStatusUpdate> statusUpdates;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TxStatusUpdate {
        private String txId;
        private LedgerDispatchStatus status;
    }

}
