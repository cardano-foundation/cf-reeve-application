package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ScheduledIngestionEvent {

    private FilteringParameters filteringParameters;

    private String initiator;

}
