package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;

import java.util.Set;

/**
 * Event used after data has been sent or updated on the blockchain
 *
 * @param statusUpdates
 */
public record LedgerUpdatedEvent(String organisationId,
                                 Set<TxStatusUpdate> statusUpdates) {

    public record TxStatusUpdate(String txId, LedgerDispatchStatus status) {}

}
