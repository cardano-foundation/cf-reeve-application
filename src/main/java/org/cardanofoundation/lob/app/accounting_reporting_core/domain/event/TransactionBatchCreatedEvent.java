package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
@AllArgsConstructor
@Builder
@Getter
public class TransactionBatchCreatedEvent {

    private String batchId;
    private String instanceId;
    private UserExtractionParameters userExtractionParameters;
    private SystemExtractionParameters systemExtractionParameters;

}
