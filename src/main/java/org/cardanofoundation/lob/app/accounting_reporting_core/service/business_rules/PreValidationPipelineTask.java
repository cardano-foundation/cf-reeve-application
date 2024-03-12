package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

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
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@Slf4j
public class PreValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(this::amountsFcyCheck)
                .map(this::amountsLcyCheck)
                .map(this::balanceZerosOutLcyCheck)
                .map(this::balanceZerosOutFcyCheck)
                .toList();

        val newViolations = new HashSet<Violation>();
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

    public TransactionWithViolations amountsFcyCheck(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        if (tx.getTransactionType() != FxRevaluation) {
            for (val txItem : tx.getItems()) {
                if (txItem.getAmountLcy().signum() != 0 && txItem.getAmountFcy().signum() == 0) {
                    val v = Violation.create(
                            ERROR,
                            tx.getOrganisation().getId(),
                            tx.getId(),
                            txItem.getId(),
                            AMOUNT_FCY_IS_ZERO,
                            ConversionsPipelineTask.class.getName(),
                            Map.of(
                                    "transactionNumber", tx.getInternalTransactionNumber(),
                                    "amountFcy", txItem.getAmountFcy(),
                                    "amountLcy", txItem.getAmountLcy()
                            )
                    );

                    violations.add(v);
                }
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return TransactionWithViolations.create(tx.toBuilder().validationStatus(FAILED).build(), violations);
    }

    public TransactionWithViolations amountsLcyCheck(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val violations = new HashSet<Violation>();

        for (val txItem : tx.getItems()) {
            if (txItem.getAmountLcy().signum() == 0 && txItem.getAmountFcy().signum() != 0) {
                val v = Violation.create(
                        ERROR,
                        tx.getOrganisation().getId(),
                        tx.getId(),
                        txItem.getId(),
                        AMOUNT_LCY_IS_ZERO,
                        ConversionsPipelineTask.class.getName(),
                        Map.of(
                                "transactionNumber", tx.getInternalTransactionNumber(),
                                "amountFcy", txItem.getAmountFcy(),
                                "amountLcy", txItem.getAmountLcy()
                        )
                );

                violations.add(v);
            }
        }

        if (violations.isEmpty()) {
            return violationTransaction;
        }

        return TransactionWithViolations.create(tx.toBuilder().validationStatus(FAILED).build(), violations);
    }

    public TransactionWithViolations balanceZerosOutLcyCheck(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

        val txItems = tx.getItems();
        val lcySum = txItems.stream().map(TransactionItem::getAmountLcy).reduce(ZERO, BigDecimal::add);

        if (lcySum.signum() != 0) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    LCY_BALANCE_MUST_BE_ZERO,
                    ConversionsPipelineTask.class.getName(),
                    Map.of("transactionNumber", tx.getInternalTransactionNumber())
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }


        return violationTransaction;
    }

    public TransactionWithViolations balanceZerosOutFcyCheck(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();
        val txItems = tx.getItems();

        val fcySum = txItems.stream()
                .map(TransactionItem::getAmountFcy)
                .reduce(ZERO, BigDecimal::add);

        if (fcySum.signum() != 0) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    FCY_BALANCE_MUST_BE_ZERO,
                    ConversionsPipelineTask.class.getName(),
                    Map.of("transactionNumber", tx.getInternalTransactionNumber())
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        return violationTransaction;
    }

}
