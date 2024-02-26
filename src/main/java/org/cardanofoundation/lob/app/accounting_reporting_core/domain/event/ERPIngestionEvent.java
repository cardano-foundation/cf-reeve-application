package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.UUID;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 *
 * @param organisationTransactions
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@DomainEvent
public class ERPIngestionEvent {

    private UUID lotId;
    private String initiator;
    private FilteringParameters filteringParameters;
    private OrganisationTransactions organisationTransactions;

}
