package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class PostCleansingPipelineTask implements PipelineTask {

    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines.entries();

        val passed = txLines.stream()
                .filter(this::isDebitAccountNotTheSameAsCreditAccount)
                .toList();

        val filtered = Sets.difference(Set.copyOf(txLines), Set.copyOf(passed))
                .stream()
                .toList();

        log.warn("Filtered lines: {}", filtered.size());
        for (val transactionLine : filtered) {
            log.warn("Filtered line, tx line id: {}, tx id:{}", transactionLine.getId(), transactionLine.getInternalTransactionNumber());
        }

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passed),
                ignoredTransactionLines,
                new TransactionLines(passedTransactionLines.organisationId(), Stream.concat(filteredTransactionLines.entries().stream(), filtered.stream()).toList()),
                violations
        );
    }

    private boolean isDebitAccountSameAsCreditAccount(TransactionLine transactionLine) {
        return transactionLine.getAccountCodeDebit().equals(transactionLine.getAccountCodeCredit());
    }

    private boolean isDebitAccountNotTheSameAsCreditAccount(TransactionLine transactionLine) {
        return !isDebitAccountSameAsCreditAccount(transactionLine);
    }

}
