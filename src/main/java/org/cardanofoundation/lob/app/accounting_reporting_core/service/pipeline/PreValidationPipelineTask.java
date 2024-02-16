package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class PreValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> violations) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(Transaction.WithPossibleViolations::create)
                .map(this::amountsFcyCheck)
                .map(this::amountsLcyCheck)
                .map(this::balanceZerosOutLcyCheck)
                .map(this::balanceZerosOutFcyCheck)
                .map(this::isEmpty)
                .toList();

        val newViolations = new HashSet<>(violations);
        val finalTransactions = new LinkedHashSet<Transaction>();

        for (val transactions : transactionsWithPossibleViolation) {
            finalTransactions.add(transactions.transaction());
            newViolations.addAll(transactions.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), finalTransactions),
                ignoredTransactions,
                newViolations
        );
    }

    private Transaction.WithPossibleViolations isEmpty(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        if (tx.getTransactionItems().isEmpty()) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    "TRANSACTION_ITEMS_EMPTY",
                    Map.of()
            );

            return Transaction.WithPossibleViolations.create(tx.toBuilder().validationStatus(FAILED).build(), v);
        }

        // pass through
        return violationTransaction;
    }

    public Transaction.WithPossibleViolations amountsFcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getTransactionType() != FxRevaluation) {
            for (val txItem : tx.getTransactionItems()) {
                if (txItem.getAmountLcy().signum() != 0 && txItem.getAmountFcy().signum() == 0) {
                    val v = Violation.create(
                            Violation.Priority.HIGH,
                            Violation.Type.FATAL,
                            tx.getOrganisation().getId(),
                            tx.getId(),
                            txItem.getId(),
                            "AMOUNT_FCY_IS_ZERO",
                            Map.of("amountFcy", txItem.getAmountFcy(), "amountLcy", txItem.getAmountLcy())
                    );

                    violations.add(v);
                }
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return Transaction.WithPossibleViolations.create(tx.toBuilder().validationStatus(FAILED).build(), violations);
    }

    public Transaction.WithPossibleViolations amountsLcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        for (val txItem : tx.getTransactionItems()) {
            if (txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() != 0) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        "AMOUNT_LCY_IS_ZERO",
                        Map.of("amountFcy", txItem.getAmountFcy(), "amountLcy", txItem.getAmountLcy())
                );

                violations.add(v);
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return Transaction.WithPossibleViolations.create(tx.toBuilder().validationStatus(FAILED).build(), violations);
    }

    public Transaction.WithPossibleViolations balanceZerosOutLcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val txItems = tx.getTransactionItems();
        val lcySum = txItems.stream().map(TransactionItem::getAmountLcy).reduce(ZERO, BigDecimal::add);

        if (lcySum.signum() != 0) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    "LCY_BALANCE_MUST_BE_ZERO",
                    Map.of()
            );

            return Transaction.WithPossibleViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }


        return violationTransaction;
    }

    public Transaction.WithPossibleViolations balanceZerosOutFcyCheck(Transaction.WithPossibleViolations violationTransaction) {
        val tx = violationTransaction.transaction();
        val txItems = tx.getTransactionItems();

        val fcySum = txItems.stream().map(TransactionItem::getAmountFcy).reduce(ZERO, BigDecimal::add);
        if (fcySum.signum() != 0) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    "FCY_BALANCE_MUST_ZERO",
                    Map.of()
            );

            return Transaction.WithPossibleViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        return violationTransaction;
    }

}
