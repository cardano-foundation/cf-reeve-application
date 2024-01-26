package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 *
 * @param transactionLines
 */
public record ERPIngestionEvent(
        String initiator,
        FilteringParameters filteringParameters,
        TransactionLines transactionLines) {
}
