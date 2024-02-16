package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;

import java.util.UUID;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 *
 * @param organisationTransactions
 */
public record ERPIngestionEvent(
        UUID extractionId,
        String initiator,
        FilteringParameters filteringParameters,
        OrganisationTransactions organisationTransactions) {
}
