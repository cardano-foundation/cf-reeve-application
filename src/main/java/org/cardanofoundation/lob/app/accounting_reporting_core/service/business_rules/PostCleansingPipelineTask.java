package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PostCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> allViolationUntilNow) {

        val passedTransactions = passedOrganisationTransactions.transactions().stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
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

    private TransactionWithViolations debitAccountCheck(TransactionWithViolations transactionLineWithViolation) {
        val tx = transactionLineWithViolation.transaction();

        // we accept only transaction items that are NOT sending to the same account, if they are we discard them
        val newItems = tx.getTransactionItems()
                .stream()
                .filter(txItem -> !txItem.getAccountCodeDebit().equals(txItem.getAccountCodeCredit()))
                .collect(Collectors.toSet());

        return TransactionWithViolations.create(tx
                .toBuilder()
                .transactionItems(newItems)
                .build()
        );
    }

}
