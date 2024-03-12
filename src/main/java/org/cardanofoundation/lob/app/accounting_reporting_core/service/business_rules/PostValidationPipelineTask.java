package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class PostValidationPipelineTask implements PipelineTask {

    private final Validator validator;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(tx -> new AccountCodeDebitCheckTaskItem(this).run(tx))
                .map(tx -> new AccountCodeCreditCheckTaskItem(this).run(tx))
                .map(tx -> new DocumentMustBePresentTaskItem(this).run(tx))
                .map(tx -> new NoTransactionItemsTaskItem(this).run(tx))
                .map(tx -> new SanityCheckFieldsTaskItem(this, validator).run(tx))
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

}
