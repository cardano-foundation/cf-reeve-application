package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;


import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BusinessRuleViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;

import java.util.Set;

public interface BusinessRulesValidator {

    Set<BusinessRuleViolation> validate(String organisationId, TransactionLines transactionLines);

}
