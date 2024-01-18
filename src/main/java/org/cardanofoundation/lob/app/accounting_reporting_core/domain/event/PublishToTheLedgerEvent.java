package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactionData;

public record PublishToTheLedgerEvent(String organisationId, OrganisationTransactionData txData) {
}
