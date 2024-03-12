package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.DebitAccountCheckTaskItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.TxItemsCollapsingTaskItem;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class PostCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> allViolationUntilNow) {

        val passedTransactions = passedOrganisationTransactions.transactions().stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(txWithViolations -> new DebitAccountCheckTaskItem().run(txWithViolations))
                .map(txWithViolations -> new TxItemsCollapsingTaskItem().run(txWithViolations))
                .toList();

        val newViolations = new HashSet<Violation>();
        val finalTransactions = new HashSet<Transaction>();

        for (val transaction : passedTransactions) {
            finalTransactions.add(transaction.transaction());
            newViolations.addAll(transaction.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedOrganisationTransactions.organisationId(), finalTransactions),
                ignoredOrganisationTransactions,
                newViolations
        );
    }

}
