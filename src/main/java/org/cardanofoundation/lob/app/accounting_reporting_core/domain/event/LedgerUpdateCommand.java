package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;

/**
 *
 * @param organisationId
 * @param txData
 */
public record LedgerUpdateCommand(String organisationId,
                                  TransactionLines txData) {
}
