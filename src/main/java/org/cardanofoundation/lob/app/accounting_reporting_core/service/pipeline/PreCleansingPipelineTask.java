package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.Set;

@Slf4j
public class PreCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines.entries();

        val passed = txLines.stream()
                .map(TransactionLine.WithPossibleViolation::create)
                .map(this::zeroBalanceCheck)
                .toList();

        val passedTxLines = passed.stream()
                .map(TransactionLine.WithPossibleViolation::transactionLine)
                .toList();

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passedTxLines),
                ignoredTransactionLines,
                violations
        );
    }

    private TransactionLine.WithPossibleViolation zeroBalanceCheck(TransactionLine.WithPossibleViolation transactionLineWithViolation) {
        val transactionLine = transactionLineWithViolation.transactionLine();

        if (transactionLine.getAmountLcy().signum() == 0 && transactionLine.getAmountFcy().signum() == 0) {
            return TransactionLine.WithPossibleViolation.create(transactionLine
                    .toBuilder()
                    .validationStatus(ValidationStatus.DISCARDED)
                    .build());
        }

        return transactionLineWithViolation;
    }

}
