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
                .map(Transaction.WithPossibleViolation::create)
                .map(this::balanceZerosOutLcyCheck)
                .map(this::balanceZerosOutFcyCheck)
                .map(this::amountsFcyCheck)
                .map(this::amountsLcyCheck)
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

    public Transaction.WithPossibleViolation amountsFcyCheck(Transaction.WithPossibleViolation violationTransaction) {
        val transaction = violationTransaction.transaction();

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

                    return Transaction.WithPossibleViolation.create(transaction
                                    .toBuilder()
                                    .transactionNumber(transaction.getTransactionNumber())
                                    .transactionLines(transaction.getTransactionLines().stream()
                                            .map(txLine -> txLine.toBuilder()
                                                    .validationStatus(FAILED)
                                                    .build())
                                            .toList())
                                    .build(),
                            Set.of(v));
                }
            }
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolation amountsLcyCheck(Transaction.WithPossibleViolation violationTransaction) {
        val transaction = violationTransaction.transaction();

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

                return Transaction.WithPossibleViolation.create(transaction
                                .toBuilder()
                                .transactionNumber(transaction.getTransactionNumber())
                                .transactionLines(transaction.getTransactionLines().stream()
                                        .map(txLine -> txLine.toBuilder()
                                                .validationStatus(FAILED)
                                                .build())
                                        .toList())
                                .build(),
                        Set.of(v));
            }
        }

        return violationTransaction;
    }

    public Transaction.WithPossibleViolation balanceZerosOutLcyCheck(Transaction.WithPossibleViolation violationTransaction) {
        val transaction = violationTransaction.transaction();

        if (transaction.getTransactionLines().size() >= 2) {
            val transactionLines = transaction.getTransactionLines();

            val lcySum = transactionLines.stream().map(TransactionLine::getAmountLcy).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (lcySum.signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transaction.getTransactionNumber(),
                        "LCY_BALANCE_MUST_BE_ZERO"
                );

                return Transaction.WithPossibleViolation.create(transaction
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

    public Transaction.WithPossibleViolation balanceZerosOutFcyCheck(Transaction.WithPossibleViolation violationTransaction) {
        val transaction = violationTransaction.transaction();

        if (transaction.getTransactionLines().size() >= 2) {
            val transactionLines = transaction.getTransactionLines();

            val fcySum = transactionLines.stream().map(TransactionLine::getAmountFcy).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (fcySum.signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transaction.getTransactionNumber(),
                        "FCY_BALANCE_MUST_ZERO"
                );

                return Transaction.WithPossibleViolation.create(transaction
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
