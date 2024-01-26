package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;

public record ScheduledIngestionEvent(
        FilteringParameters filteringParameters,
        String initiator) {
}
