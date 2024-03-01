package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.*;

@RequiredArgsConstructor
@Slf4j
public class PostValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::accountCodeDebitCheck)
                .map(this::accountCodeCreditCheck)
                .map(this::documentMustBePresentCheck)
                .map(this::checkTransactionItemsEmpty)
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

    private Transaction.WithPossibleViolations checkTransactionItemsEmpty(Transaction.WithPossibleViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getTransactionItems().isEmpty()) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    TRANSACTION_ITEMS_EMPTY,
                    Map.of()
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
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
                        ACCOUNT_CODE_DEBIT_IS_EMPTY,
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

        if (tx.getTransactionType() == Journal) {
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
                        ACCOUNT_CODE_CREDIT_IS_EMPTY,
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

    private Transaction.WithPossibleViolations documentMustBePresentCheck(Transaction.WithPossibleViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getDocument().isEmpty()) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    DOCUMENT_MUST_BE_PRESENT,
                    Map.of()
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

}
