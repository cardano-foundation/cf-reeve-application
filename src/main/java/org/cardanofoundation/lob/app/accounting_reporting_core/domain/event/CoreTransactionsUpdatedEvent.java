package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import java.util.List;

/**
 * Event is used to notify that internal accounting core has successfully updated the transaction lines
 *
 * @param txLinesId
 */
public record CoreTransactionsUpdatedEvent(String organisationId,
                                           List<String> txLinesId) {
}
