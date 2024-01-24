package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BusinessRuleViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DefaultBusinessRulesValidator implements BusinessRulesValidator {

    private final List<BusinessRulesValidator> businessRulesValidators = new ArrayList<>();

    @PostConstruct
    public void init() {
        businessRulesValidators.add(new VatRateValidator());
    }

    @Override
    public Set<BusinessRuleViolation> validate(String organisationId,
                                               TransactionLines transactionLines) {
        var allViolations = new HashSet<BusinessRuleViolation>();

        businessRulesValidators.forEach(validator -> {
            allViolations.addAll(validator.validate(organisationId, transactionLines));
        });

        return Set.copyOf(allViolations);
    }

}
