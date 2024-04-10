package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DefaultBusinessRulesPipelineProcessor implements BusinessRulesPipelineProcessor {

    private final List<PipelineTask> pipelineTasks;

    @Override
    public TransformationResult run(final OrganisationTransactions initialOrganisationTransactions,
                                    final OrganisationTransactions initialIgnoredTransactions) {

        var currentOrganisationTransactions = initialOrganisationTransactions;
        var currentIgnoredTransactions = initialIgnoredTransactions;

        for (PipelineTask pipelineTask : pipelineTasks) {
            val transformationResult = pipelineTask.run(currentOrganisationTransactions, currentIgnoredTransactions);

            currentOrganisationTransactions = transformationResult.passedTransactions();
            currentIgnoredTransactions = transformationResult.ignoredTransactions();
        }

        return new TransformationResult(currentOrganisationTransactions, currentIgnoredTransactions);
    }

}
