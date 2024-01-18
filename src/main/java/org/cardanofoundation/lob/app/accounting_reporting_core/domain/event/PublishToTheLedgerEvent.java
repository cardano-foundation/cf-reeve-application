package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;

public record PublishToTheLedgerEvent(String organisationId, TransactionData txData) {
}
