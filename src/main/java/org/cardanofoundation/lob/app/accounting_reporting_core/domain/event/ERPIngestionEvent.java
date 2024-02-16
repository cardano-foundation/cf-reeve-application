package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

import java.util.UUID;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 *
 * @param organisationTransactions
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ERPIngestionEvent {

    private UUID extractionId;
    private String initiator;
    private FilteringParameters filteringParameters;
    private OrganisationTransactions organisationTransactions;

}
