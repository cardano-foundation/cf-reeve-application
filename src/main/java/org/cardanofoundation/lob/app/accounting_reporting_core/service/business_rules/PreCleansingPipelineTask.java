package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import io.vavr.Predicates;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PreCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val passedTransactions = passedOrganisationTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(this::discardZeroBalanceTransactionItems)
                .collect(Collectors.toSet());

        val newViolations = new HashSet<Violation>();
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

    private TransactionWithViolations discardZeroBalanceTransactionItems(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val newItems = tx.getTransactionItems()
                .stream()
                .filter(Predicates.not(txItem -> txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() == 0))
                .collect(Collectors.toSet());

        return TransactionWithViolations.create(
                tx.toBuilder()
                        .transactionItems(newItems)
                        .build()
        );
    }

}
