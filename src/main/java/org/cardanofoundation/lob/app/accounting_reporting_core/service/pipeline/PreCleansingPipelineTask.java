package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import io.vavr.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PreCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> violations) {
        val passedTransactions = passedOrganisationTransactions.transactions()
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::discardZeroBalanceTransactionItems)
                .collect(Collectors.toSet());

        val newViolations = new HashSet<>(violations);
        val finalTransactions = new LinkedHashSet<Transaction>();

        for (val transactions : passedTransactions) {
            finalTransactions.add(transactions.transaction());
            newViolations.addAll(transactions.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedOrganisationTransactions.organisationId(), finalTransactions),
                ignoredOrganisationTransactions,
                newViolations
        );
    }

    private Transaction.WithPossibleViolations discardZeroBalanceTransactionItems(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val newItems = tx.getTransactionItems()
                .stream()
                .filter(Predicates.not(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0))
                .collect(Collectors.toSet());

        return Transaction.WithPossibleViolations.create(
                tx.toBuilder()
                        .transactionItems(newItems)
                        .build()
        );
    }

}
