package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.PipelineTaskItem;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Setter
public class DefaultPipelineTask implements PipelineTask {

    private final List<PipelineTaskItem> items;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    ProcessorFlags flags) {
        if (passedTransactions.transactions().isEmpty()) {
            return new TransformationResult(passedTransactions, ignoredTransactions);
        }

        val txs = passedTransactions.transactions()
                .stream()
                .map(this::runTaskItems)
                .collect(Collectors.toSet());

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), txs),
                ignoredTransactions
        );
    }

    private Transaction runTaskItems(Transaction transaction) {
        return items.stream()
                .reduce(
                        transaction,
                        (tx, taskItem) -> taskItem.run(tx),
                        (tx1, tx2) -> tx2); // TODO combiner???
    }

}
