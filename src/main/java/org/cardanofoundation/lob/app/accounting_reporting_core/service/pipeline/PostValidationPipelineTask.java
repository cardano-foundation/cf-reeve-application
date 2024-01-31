package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

public class PostValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {
        val converted = passedTransactionLines.entries().stream()
                .map(TransactionLine.WithPossibleViolation::create)
                .map(this::accountCodeDebitCheck)
                .map(this::accountCodeCreditCheck)
                .toList();

        val newViolations = new HashSet<>(violations);

        converted.forEach(p -> newViolations.addAll(p.violations()));

        val passedTxLines = converted.stream()
                .map(TransactionLine.WithPossibleViolation::transactionLine)
                .toList();

        val checkedTxLines = new TransactionLines(passedTransactionLines.organisationId(), passedTxLines);

        return new TransformationResult(
                checkedTxLines,
                ignoredTransactionLines,
                filteredTransactionLines,
                newViolations
        );
    }

    private TransactionLine.WithPossibleViolation accountCodeDebitCheck(TransactionLine.WithPossibleViolation withPossibleViolation) {
        val transactionLine = withPossibleViolation.transactionLine();

        if (transactionLine.getTransactionType() != TransactionType.FxRevaluation) {
            if (transactionLine.getAccountCodeDebit().isEmpty())  {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "ACCOUNT_CODE_DEBIT_IS_EMPTY"
                );

                return TransactionLine.WithPossibleViolation.create(transactionLine
                                .toBuilder()
                                .validationStatus(FAILED)
                                .build(),
                        Set.of(v));
            }
        }

        return withPossibleViolation;
    }

    private TransactionLine.WithPossibleViolation accountCodeCreditCheck(TransactionLine.WithPossibleViolation withPossibleViolation) {
        val transactionLine = withPossibleViolation.transactionLine();

        if (transactionLine.getTransactionType() != TransactionType.Journal) {
            if (transactionLine.getAccountCodeCredit().isEmpty())  {
                val v = Violation.create(
                        Violation.Priority.NORMAL,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "ACCOUNT_CODE_CREDIT_IS_EMPTY"
                );

                return TransactionLine.WithPossibleViolation.create(transactionLine
                                .toBuilder()
                                .validationStatus(FAILED)
                                .build(),
                        Set.of(v));
            }
        }

        return withPossibleViolation;
    }

}
