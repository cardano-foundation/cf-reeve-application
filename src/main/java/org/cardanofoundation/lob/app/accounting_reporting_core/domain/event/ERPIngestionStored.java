package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.jmolecules.event.annotation.DomainEvent;

@AllArgsConstructor
@Builder
@DomainEvent
@Getter
public class ERPIngestionStored {

    private String batchId;
    private String organisationId;
    private String initiator;
    private String instanceId;
    private UserExtractionParameters userExtractionParameters;
    private SystemExtractionParameters systemExtractionParameters;

}
