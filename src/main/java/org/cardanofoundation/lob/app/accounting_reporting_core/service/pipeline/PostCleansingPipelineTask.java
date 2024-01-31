package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.Set;

@Slf4j
public class PostCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines.entries();

        val passed = txLines.stream()
                .map(TransactionLine.WithPossibleViolation::create)
                .map(this::debitAccountCheck)
                .toList();

        val passedTxLines = passed.stream().map(TransactionLine.WithPossibleViolation::transactionLine).toList();

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passedTxLines),
                ignoredTransactionLines,
                violations
        );
    }

    private TransactionLine.WithPossibleViolation debitAccountCheck(TransactionLine.WithPossibleViolation transactionLineWithViolation) {
        val transactionLine = transactionLineWithViolation.transactionLine();

        if (transactionLine.getAccountCodeDebit().equals(transactionLine.getAccountCodeCredit())) {
            return TransactionLine.WithPossibleViolation.create(transactionLine
                    .toBuilder()
                    .validationStatus(ValidationStatus.DISCARDED)
                    .build());
        }

        return transactionLineWithViolation;
    }

}
