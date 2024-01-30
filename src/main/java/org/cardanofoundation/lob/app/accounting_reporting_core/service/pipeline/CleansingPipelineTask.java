package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import com.google.common.collect.Sets;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.Set;
import java.util.stream.Stream;

public class CleansingPipelineTask implements PipelineTask {

    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines.entries();

        val passed = txLines.stream()
                .filter(this::isNonZeroBalance)
                .toList();

        val filtered = Sets.difference(Set.copyOf(txLines), Set.copyOf(passed))
                .stream()
                .toList();

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passed),
                ignoredTransactionLines,
                new TransactionLines(passedTransactionLines.organisationId(), Stream.concat(filteredTransactionLines.entries().stream(), filtered.stream()).toList()),
                violations
        );
    }

    private boolean isZeroBalance(TransactionLine transactionLine) {
        return transactionLine.getAmountLcy().signum() == 0
                && transactionLine.getAmountFcy().signum() == 0;
    }

    private boolean isNonZeroBalance(TransactionLine transactionLine) {
        return !isZeroBalance(transactionLine);
    }

}
