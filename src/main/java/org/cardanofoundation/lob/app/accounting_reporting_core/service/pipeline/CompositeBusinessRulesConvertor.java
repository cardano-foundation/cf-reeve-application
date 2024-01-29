package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class CompositeBusinessRulesConvertor implements BusinessRulesConvertor {

    private final List<BusinessRulesConvertor> businessRulesConvertors = new ArrayList<>();

    @PostConstruct
    public void init() {
    }

    @Override
    public TransformationResult runPreValidation(TransactionLines transactionLines, TransactionLines ignoredTransactionLines) {
        var allViolations = new HashSet<Violation>();

//        businessRulesConvertors.stream().map(validator -> validator.convert(transactionLines))
//                .forEach(conversionResult -> allViolations.addAll(conversionResult.businessRuleViolations()));
//
//        businessRulesConvertors.forEach(validator -> {
//            val conversionResult = validator.convert(transactionLines);
//
//            allViolations.addAll(validator.convert(transactionLines));
//        });

        return TransformationResult.create(transactionLines);
    }

    @Override
    public TransformationResult runPostValidation(TransactionLines transactionLines, TransactionLines ignoredTransactionLines) {
        return TransformationResult.create(transactionLines);
    }

}
