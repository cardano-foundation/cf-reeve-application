package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;

import java.util.UUID;

/**
 *
 * @param organisationId
 * @param transactionLines
 */
public record LedgerUpdateCommand(UUID uploadId,
                                 TransactionLines transactionLines) {

    public static LedgerUpdateCommand create(TransactionLines transactionLines) {
        return new LedgerUpdateCommand(UUID.randomUUID(), transactionLines);
    }

}
