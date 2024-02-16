package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@RequiredArgsConstructor
@Slf4j
public class PostValidationPipelineTask implements PipelineTask {

    private final Validator validator;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> violations) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::sanityCheckFields)
                .map(this::accountCodeDebitCheck)
                .map(this::accountCodeCreditCheck)
                .toList();

        val newViolations = new HashSet<>(violations);
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

    private Transaction.WithPossibleViolations sanityCheckFields(Transaction.WithPossibleViolations withPossibleViolations) {
        val transaction = withPossibleViolations.transaction();
        val violations = new HashSet<Violation>();

        val errors = validator.validate(transaction);

        if (!errors.isEmpty()) {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.FATAL,
                    transaction.getOrganisation().getId(),
                    transaction.getId(),
                    "INVALID_TRANSACTION",
                    Map.of()
                    //Map.of("errors", errors)
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(transaction.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

    private Transaction.WithPossibleViolations accountCodeDebitCheck(Transaction.WithPossibleViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getTransactionType() == TransactionType.FxRevaluation) {
            return withPossibleViolations;
        }

        for (val txItem : tx.getTransactionItems()) {
            if (txItem.getAccountCodeDebit().isEmpty())  {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        "ACCOUNT_CODE_DEBIT_IS_EMPTY",
                        Map.of()
                );

                violations.add(v);
            }
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

    private Transaction.WithPossibleViolations accountCodeCreditCheck(Transaction.WithPossibleViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        if (tx.getTransactionType() == TransactionType.Journal) {
            return withPossibleViolations;
        }

        val violations = new HashSet<Violation>();
        for (val txItem : tx.getTransactionItems()) {
            if (txItem.getAccountCodeCredit().isEmpty())  {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        "ACCOUNT_CODE_CREDIT_IS_EMPTY",
                        Map.of()
                );

                violations.add(v);
            }
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

}
