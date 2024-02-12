package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@RequiredArgsConstructor
@Slf4j
public class PostValidationPipelineTask implements PipelineTask {

    private final Validator validator;

    @Override
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val newViolations = new HashSet<>(violations);
        val transactions = passedTransactionLines.toTransactions();

        val transactionsWithPossibleViolation = transactions
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::sanityCheckFields)
                .map(this::accountCodeDebitCheck)
                .map(this::accountCodeCreditCheck)
                .toList();

        val finalTxLinesList = new ArrayList<TransactionLine>();

        transactionsWithPossibleViolation.forEach(p -> {
            finalTxLinesList.addAll(p.transaction().getTransactionLines());
            newViolations.addAll(p.violations());
        });

        val finalTxLines = new TransactionLines(passedTransactionLines.organisationId(), finalTxLinesList);

        return new TransformationResult(
                finalTxLines,
                ignoredTransactionLines,
                newViolations
        );
    }

    private Transaction.WithPossibleViolations sanityCheckFields(Transaction.WithPossibleViolations withPossibleViolations) {
        val transaction = withPossibleViolations.transaction();
        val violations = new HashSet<Violation>();

        for (val txLine : transaction.getTransactionLines()) {
            val errors = validator.validate(txLine);

            if (!errors.isEmpty()) {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        txLine.getId(),
                        txLine.getInternalTransactionNumber(),
                        "INVALID_TRANSACTION_LINE",
                        Map.of("errors", errors)
                );

                violations.add(v);
            }
        }

        if (violations.isEmpty()) {
            return withPossibleViolations;
        }

        return Transaction.WithPossibleViolations.create(transaction
                        .toBuilder()
                        .orgTransactionNumber(new OrgTransactionNumber(transaction.getOrgTransactionNumber().organisationId(), transaction.getOrgTransactionNumber().transactionNumber()))
                        .transactionLines(transaction.getTransactionLines().stream()
                                .map(txLine -> txLine.toBuilder()
                                        .validationStatus(FAILED)
                                        .build())
                                .toList())
                        .build(),
                violations);
    }

    private Transaction.WithPossibleViolations accountCodeDebitCheck(Transaction.WithPossibleViolations withPossibleViolations) {
        val transaction = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();
        for (val transactionLine : transaction.getTransactionLines()) {

            if (transactionLine.getTransactionType() != TransactionType.FxRevaluation) {
                if (transactionLine.getAccountCodeDebit().isEmpty())  {
                    val v = Violation.create(
                            Violation.Priority.NORMAL,
                            Violation.Type.FATAL,
                            transactionLine.getId(),
                            transactionLine.getInternalTransactionNumber(),
                            "ACCOUNT_CODE_DEBIT_IS_EMPTY"
                    );

                    violations.add(v);
                }
            }
        }

        if (violations.isEmpty()) {
            return withPossibleViolations;
        }

        return Transaction.WithPossibleViolations.create(transaction
                        .toBuilder()
                        .orgTransactionNumber(new OrgTransactionNumber(transaction.getOrgTransactionNumber().organisationId(), transaction.getOrgTransactionNumber().transactionNumber()))
                        .transactionLines(transaction.getTransactionLines().stream()
                                .map(txLine -> txLine.toBuilder()
                                        .validationStatus(FAILED)
                                        .build())
                                .toList())
                        .build(),
                violations);
    }

    private Transaction.WithPossibleViolations accountCodeCreditCheck(Transaction.WithPossibleViolations withPossibleViolations) {
        val transaction = withPossibleViolations.transaction();

        val violations = new HashSet<Violation>();
        for (val transactionLine : transaction.getTransactionLines()) {
            if (transactionLine.getTransactionType() != TransactionType.Journal) {
                if (transactionLine.getAccountCodeCredit().isEmpty())  {
                    val v = Violation.create(
                            Violation.Priority.NORMAL,
                            Violation.Type.FATAL,
                            transactionLine.getId(),
                            transactionLine.getInternalTransactionNumber(),
                            "ACCOUNT_CODE_CREDIT_IS_EMPTY"
                    );

                    violations.add(v);
                }
            }
        }

        if (violations.isEmpty()) {
            return withPossibleViolations;
        }

        return Transaction.WithPossibleViolations.create(transaction
                        .toBuilder()
                        .orgTransactionNumber(new OrgTransactionNumber(transaction.getOrgTransactionNumber().organisationId(), transaction.getOrgTransactionNumber().transactionNumber()))
                        .transactionLines(transaction.getTransactionLines().stream()
                                .map(txLine -> txLine.toBuilder()
                                        .validationStatus(FAILED)
                                        .build())
                                .toList())
                        .build(),
                violations);
    }

}
