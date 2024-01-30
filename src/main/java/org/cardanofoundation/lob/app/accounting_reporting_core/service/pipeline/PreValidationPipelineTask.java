package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class PreValidationPipelineTask implements PipelineTask {

    @Override
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {

        val converted = passedTransactionLines.entries().stream()
                .map(TransactionLine.WithPossibleViolation::create)
                .map(this::amountsLcyAmountsFcyChecks)
                .toList();

        val newViolations = converted.stream()
                .filter(p -> p.violation().isPresent())
                .map(p -> p.violation().orElseThrow())
                .collect(toSet());

        val passedTxLines = converted.stream()
                .map(TransactionLine.WithPossibleViolation::transactionLine)
                .toList();

        return new TransformationResult(
                new TransactionLines(passedTransactionLines.organisationId(), passedTxLines),
                ignoredTransactionLines,
                filteredTransactionLines,
                Stream.concat(violations.stream(), newViolations.stream()).collect(toSet())
        );
    }

    public TransactionLine.WithPossibleViolation amountsLcyAmountsFcyChecks(TransactionLine.WithPossibleViolation violationTransactionLine) {
        val transactionLine = violationTransactionLine.transactionLine();

        if (transactionLine.getTransactionType() != FxRevaluation) {
            if (!transactionLine.getAmountLcy().equals(ZERO) && transactionLine.getAmountFcy().equals(ZERO)) {
                val v = Violation.create(
                        Violation.Priority.HIGH,
                        Violation.Type.FATAL,
                        transactionLine.getId(),
                        transactionLine.getInternalTransactionNumber(),
                        "AMOUNT_FCY_IS_ZERO",
                        Map.of("amountFcy", transactionLine.getAmountFcy(), "amountLcy", transactionLine.getAmountLcy())
                );

                return TransactionLine.WithPossibleViolation.create(transactionLine
                                .toBuilder()
                                .validationStatus(FAILED)
                                .build(),
                        v);
            }
        }

        if (transactionLine.getAmountLcy().equals(ZERO) && !transactionLine.getAmountFcy().equals(ZERO)) {
            val v = Violation.create(
                    Violation.Priority.HIGH,
                    Violation.Type.FATAL,
                    transactionLine.getId(),
                    transactionLine.getInternalTransactionNumber(),
                    "AMOUNT_LCY_IS_ZERO",
                    Map.of("amountFcy", transactionLine.getAmountFcy(), "amountLcy", transactionLine.getAmountLcy())
            );

            return TransactionLine.WithPossibleViolation.create(transactionLine
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        return violationTransactionLine;
    }

}
