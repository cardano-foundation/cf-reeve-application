package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@RequiredArgsConstructor
@Slf4j
public class DefaultBusinessRulesPipelineProcessor implements BusinessRulesPipelineProcessor {

    private final List<PipelineTask> pipelineTasks;

    @Override
    public TransformationResult run(OrganisationTransactions organisationTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val allViolations = new HashSet<Violation>();

        for (val pipelineTask : pipelineTasks) {
            val transformationResult = pipelineTask.run(organisationTransactions, ignoredTransactions, allViolations);
            // TODO refactor this - we do not want to over-write passed in params (anti-pattern)
            organisationTransactions = transformationResult.organisationTransactions();

            allViolations.addAll(transformationResult.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(organisationTransactions.organisationId(), validateNotFailedTransactions(organisationTransactions)),
                ignoredTransactions,
                allViolations
        );
    }

    private static Set<Transaction> validateNotFailedTransactions(OrganisationTransactions passedTransactions) {
        return passedTransactions
                .transactions()
                .stream()
                .map(tx -> {
                    if (tx.getValidationStatus() == FAILED) {
                        return tx;
                    }

                    return tx.toBuilder()
                            .validationStatus(VALIDATED)
                            .build();
                })
                .collect(Collectors.toSet());
    }

}
