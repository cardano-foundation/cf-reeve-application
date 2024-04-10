package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ViolationEvent {

    private Transaction transaction;
    private Violation violation;

}
