package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;

public record OrgTransactionNumber(@Size(min = 1, max =  255) String organisationId,
                                   @Size(min = 1, max =  255) String transactionNumber) {
}
