package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@DomainEvent
public class LedgerUpdateCommand {

    private UUID uploadId;
    private OrganisationTransactions organisationTransactions;

    public static LedgerUpdateCommand create(OrganisationTransactions organisationTransactions) {
        return new LedgerUpdateCommand(UUID.randomUUID(), organisationTransactions);
    }

}
