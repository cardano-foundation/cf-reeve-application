package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BusinessRuleViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;

import java.util.HashSet;
import java.util.Set;

public class VatRateValidator implements BusinessRulesValidator {

    @Override
    public Set<BusinessRuleViolation> validate(String organisationId, TransactionLines transactionLines) {
        var violations = new HashSet<BusinessRuleViolation>();

        transactionLines.entries().forEach(entry -> {
            if (entry.vat().isPresent()) {
                //entry.vat().orElseThrow().vatCode()
            }

        });

        return violations;
    }

}
