package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class PreValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val newViolations = new HashSet<>(violations);
        val transactions = passedTransactionLines.toTransactions();

        val transactionsWithPossibleViolation = transactions
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::amountsFcyCheck)
                .map(this::amountsLcyCheck)
                .map(this::balanceZerosOutLcyCheck)
                .map(this::balanceZerosOutFcyCheck)
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

    public Transaction.WithPossibleViolations amountsFcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val transaction = violationTransaction.transaction();

        val violations = new HashSet<Violation>();
        for (val transactionLine : transaction.getTransactionLines()) {

            if (transactionLine.getTransactionType() != FxRevaluation) {
                if (transactionLine.getAmountLcy().signum() != 0 && transactionLine.getAmountFcy().signum() == 0) {
                    val v = Violation.create(
                            Violation.Priority.HIGH,
                            Violation.Type.FATAL,
                            transactionLine.getId(),
                            transactionLine.getInternalTransactionNumber(),
                            "AMOUNT_FCY_IS_ZERO",
                            Map.of("amountFcy", transactionLine.getAmountFcy(), "amountLcy", transactionLine.getAmountLcy())
                    );

                    violations.add(v);
                }
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return Transaction.WithPossibleViolations.create(transaction
                        .toBuilder()
                        .orgTransactionNumber(transaction.getOrgTransactionNumber())
                        .transactionLines(transaction.getTransactionLines().stream()
                                .map(txLine -> txLine.toBuilder()
                                        .validationStatus(FAILED)
                                        .build())
                                .toList())
                        .build(),
                violations);
    }

    public Transaction.WithPossibleViolations amountsLcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val transaction = violationTransaction.transaction();

        val violations = new HashSet<Violation>();
        for (val transactionLine : transaction.getTransactionLines()) {
            if (transactionLine.getAmountLcy().signum() == 0 && transactionLine.getAmountFcy().signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "AMOUNT_LCY_IS_ZERO",
                        Map.of("amountFcy", transactionLine.getAmountFcy(), "amountLcy", transactionLine.getAmountLcy())
                );

                violations.add(v);
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return Transaction.WithPossibleViolations.create(transaction
                        .toBuilder()
                        .orgTransactionNumber(transaction.getOrgTransactionNumber())
                        .transactionLines(transaction.getTransactionLines().stream()
                                .map(txLine -> txLine.toBuilder()
                                        .validationStatus(FAILED)
                                        .build())
                                .toList())
                        .build(),
                violations);
    }

    public Transaction.WithPossibleViolations balanceZerosOutLcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val transaction = violationTransaction.transaction();

        if (transaction.getTransactionLines().size() >= 2) {
            val transactionLines = transaction.getTransactionLines();

            val lcySum = transactionLines.stream().map(TransactionLine::getAmountLcy).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (lcySum.signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transaction.getOrgTransactionNumber().organisationId(),
                        transaction.getOrgTransactionNumber().transactionNumber(),
                        "LCY_BALANCE_MUST_BE_ZERO"
                );

                return Transaction.WithPossibleViolations.create(transaction
                                .toBuilder()
                                .transactionLines(transaction.getTransactionLines().stream()
                                        .map(txLine -> txLine.toBuilder()
                                                .validationStatus(FAILED)
                                                .build())
                                        .toList())
                                .build(),
                        v);
            }
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolations balanceZerosOutFcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val transaction = violationTransaction.transaction();

        if (transaction.getTransactionLines().size() >= 2) {
            val transactionLines = transaction.getTransactionLines();

            val fcySum = transactionLines.stream().map(TransactionLine::getAmountFcy).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (fcySum.signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transaction.getOrgTransactionNumber().organisationId(),
                        transaction.getOrgTransactionNumber().transactionNumber(),
                        "FCY_BALANCE_MUST_ZERO"
                );

                return Transaction.WithPossibleViolations.create(transaction
                                .toBuilder()
                                .transactionLines(transaction.getTransactionLines().stream()
                                        .map(txLine -> txLine.toBuilder()
                                                .validationStatus(FAILED)
                                                .build())
                                        .toList())
                                .build(),
                        v);
            }
        }

        return violationTransaction;
    }

}
