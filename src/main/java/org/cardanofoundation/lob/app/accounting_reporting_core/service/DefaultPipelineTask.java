package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.PipelineTaskItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Setter
public class DefaultPipelineTask implements PipelineTask {

    private List<PipelineTaskItem> items;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {

        if (passedTransactions.transactions().isEmpty()) {
            return new TransformationResult(passedTransactions, ignoredTransactions, allViolationUntilNow);
        }

        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(this::runTaskItems)
                .collect(Collectors.toSet());

        val newViolations = new HashSet<Violation>();

        val finalTransactions = new HashSet<Transaction>();

        for (val violationTransaction : transactionsWithPossibleViolation) {
            finalTransactions.add(violationTransaction.transaction());
            newViolations.addAll(violationTransaction.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), finalTransactions),
                ignoredTransactions,
                newViolations
        );
    }

    private TransactionWithViolations runTaskItems(TransactionWithViolations transaction) {
        return items.stream()
                .reduce(transaction, (tx, taskItem) -> taskItem.run(tx), (tx1, tx2) -> tx2);
    }

}
