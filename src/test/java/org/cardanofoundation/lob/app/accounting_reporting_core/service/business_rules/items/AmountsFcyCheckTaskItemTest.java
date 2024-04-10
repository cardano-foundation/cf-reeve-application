package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.AMOUNT_FCY_IS_ZERO;

class AmountsFcyCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AmountsFcyCheckTaskItem();
    }

    // Testing violation generation for non-FxRevaluation transactions with zero FCY and non-zero LCY amounts.
    @Test
    void whenFcyIsZeroAndLcyIsNonZero_thenViolationGenerated() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit) // A type different from FxRevaluation
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .amountFcy(BigDecimal.ZERO)
                                .amountLcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).isNotEmpty();
        assertThat(newTx.getViolations().iterator().next().code()).isEqualTo(AMOUNT_FCY_IS_ZERO);
    }

    // Ensuring no violations for FxRevaluation transactions regardless of FCY and LCY amounts.
    @Test
    void whenTransactionTypeIsFxRevaluation_thenNoViolations() {
        val txId = Transaction.id("2", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation) // This type should be exempt
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountFcy(BigDecimal.ZERO)
                                .amountLcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

    // Verifying no violations when both FCY and LCY amounts are non-zero.
    @Test
    void whenBothFcyAndLcyAreNonZero_thenNoViolations() {
        val txId = Transaction.id("3", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit) // Ensure this isn't FxRevaluation
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .amountFcy(BigDecimal.valueOf(50))
                                .amountLcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

    // Confirming no violations are generated when both FCY and LCY amounts are zero.
    @Test
    void whenBothFcyAndLcyAreZero_thenNoViolations() {
        val txId = Transaction.id("4", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("4")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit) // Ensure this isn't FxRevaluation
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "3"))
                                .amountFcy(BigDecimal.ZERO)
                                .amountLcy(BigDecimal.ZERO)
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

}
