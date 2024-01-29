package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import com.google.common.collect.Sets;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.math.BigDecimal.ZERO;

@Service
public class CleansingService {

    public TransformationResult run(TransactionLines incomingPassThroughTransactionLines,
                                    TransactionLines ignoredTransactionLines) {
//        val byTransactionNumber = passThroughTransactionLines.txLines()
//                .stream()
//                .collect(groupingBy(TransactionLine::getInternalTransactionNumber));

        val txLines = incomingPassThroughTransactionLines.entries();

        val passed = txLines.stream()
                .filter(this::isNonZeroBalance)
                .toList();

        val filtered = Sets.difference(Set.copyOf(txLines), Set.copyOf(passed))
                .stream()
                .toList();

        val passThroughTransactionLines = new TransactionLines(incomingPassThroughTransactionLines.organisationId(), passed);
        val filteredTransactionLines = new TransactionLines(passThroughTransactionLines.organisationId(), filtered);

        return new TransformationResult(
                passThroughTransactionLines,
                ignoredTransactionLines,
                filteredTransactionLines,
                Set.of()
        );
    }

    private boolean isZeroBalance(TransactionLine transactionLine) {
        return transactionLine.getAmountLcy().equals(ZERO)
                && transactionLine.getAmountFcy().equals(ZERO);
    }

    private boolean isNonZeroBalance(TransactionLine transactionLine) {
        return !isZeroBalance(transactionLine);
    }

}
