package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactionData;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 *
 * @param organisationTransactionData
 */
public record ERPIngestionEvent(OrganisationTransactionData organisationTransactionData) {
}
