package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

import java.util.UUID;

/**
 * @param organisationTransactions
 */
public record LedgerUpdateCommand(UUID uploadId,
                                 OrganisationTransactions organisationTransactions) {

    public static LedgerUpdateCommand create(OrganisationTransactions organisationTransactions) {
        return new LedgerUpdateCommand(UUID.randomUUID(), organisationTransactions);
    }

}
