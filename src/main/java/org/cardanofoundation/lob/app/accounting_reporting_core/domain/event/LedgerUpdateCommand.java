package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

import java.util.UUID;

/**
 * @param organisationTransactions
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LedgerUpdateCommand {

    private UUID uploadId;
    private OrganisationTransactions organisationTransactions;

    public static LedgerUpdateCommand create(OrganisationTransactions organisationTransactions) {
        return new LedgerUpdateCommand(UUID.randomUUID(), organisationTransactions);
    }

}
