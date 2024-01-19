package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;

import java.util.Map;

/**
 * Event used after data has been sent or updated on the blockchain
 *
 * @param statusUpdates
 */
public record LedgerUpdatedEvent(String organisationId,
                                 Map<String, TransactionLine.LedgerDispatchStatus> statusUpdates) {

}
