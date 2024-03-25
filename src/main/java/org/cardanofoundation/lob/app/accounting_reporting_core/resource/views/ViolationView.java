package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ViolationCode;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class ViolationView {
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type type;
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source source;
    private Optional<String> transactionItemId;
    private ViolationCode code;
    private Map<String, Object> bag;

}
