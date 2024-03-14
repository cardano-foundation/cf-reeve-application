package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BatchChunk;
import org.jmolecules.event.annotation.DomainEvent;

/**
 * Event which is used to after adapter layer (ACL layer) successfully maps the data from the ERP system to the internal accounting core model
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@DomainEvent
public class ERPIngestionEvent {

    private BatchChunk batchChunk;

}
