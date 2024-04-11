package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@RequiredArgsConstructor
@Slf4j
public class DefaultBusinessRulesPipelineProcessor implements BusinessRulesPipelineProcessor {

    private final List<PipelineTask> pipelineTasks;

    @Override
    public TransformationResult run(final OrganisationTransactions initialOrganisationTransactions,
                                    final OrganisationTransactions initialIgnoredTransactions,
                                    final ProcessorFlags flags) {
        var currentOrganisationTransactions = new OrganisationTransactions(initialOrganisationTransactions.organisationId(),
                prepareToReprocess(initialOrganisationTransactions.transactions()));

        var currentIgnoredTransactions = initialIgnoredTransactions;

        for (PipelineTask pipelineTask : pipelineTasks) {
            val transformationResult = pipelineTask.run(currentOrganisationTransactions, currentIgnoredTransactions, flags);

            currentOrganisationTransactions = transformationResult.passedTransactions();
            currentIgnoredTransactions = transformationResult.ignoredTransactions();
        }

        return new TransformationResult(currentOrganisationTransactions, currentIgnoredTransactions);
    }

    private Set<Transaction> prepareToReprocess(Set<Transaction> txs) {
        return txs.stream()
                .map(tx -> tx
                        .toBuilder()
                        .violations(Set.of())
                        .validationStatus(VALIDATED)
                        .build())
                .collect(Collectors.toSet());
    }

}
