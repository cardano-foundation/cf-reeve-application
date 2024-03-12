package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.AmountFcyBalanceZerosOutCheckTaskItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.AmountLcyBalanceZerosOutCheckTaskItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.AmountsFcyCheckTaskItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.AmountsLcyCheckTaskItem;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class PreValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(tx -> new AmountsFcyCheckTaskItem(this).run(tx))
                .map(tx -> new AmountsLcyCheckTaskItem(this).run(tx))
                .map(tx -> new AmountLcyBalanceZerosOutCheckTaskItem(this).run(tx))
                .map(tx -> new AmountFcyBalanceZerosOutCheckTaskItem(this).run(tx))
                .toList();

        val newViolations = new HashSet<Violation>();
        val finalTransactions = new LinkedHashSet<Transaction>();

        for (val transactions : transactionsWithPossibleViolation) {
            finalTransactions.add(transactions.transaction());
            newViolations.addAll(transactions.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), finalTransactions),
                ignoredTransactions,
                newViolations
        );
    }

}
