package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
public class PostCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions) {

        val passedTransactions = passedOrganisationTransactions.transactions().stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::debitAccountCheck)
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

    private Transaction.WithPossibleViolations debitAccountCheck(Transaction.WithPossibleViolations transactionLineWithViolation) {
        val tx = transactionLineWithViolation.transaction();

        val newItems = tx.getTransactionItems()
                .stream()
                .filter(txItem -> !txItem.getAccountCodeRefDebit().equals(txItem.getAccountCodeRefCredit()))
                .collect(Collectors.toSet());

        return Transaction.WithPossibleViolations.create(tx
                .toBuilder()
                .transactionItems(newItems)
                .build()
        );
    }

}
