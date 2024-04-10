package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.AMOUNT_LCY_IS_ZERO;

class AmountsLcyCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AmountsLcyCheckTaskItem();
    }

    // Testing generation of violation for items with zero LCY and non-zero FCY amounts.
    @Test
    void whenLcyIsZeroAndFcyIsNonZero_thenViolationGenerated() {
        val txId = Transaction.id("1", "1");
        val orgId = "1";

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id(orgId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .amountLcy(BigDecimal.ZERO)
                                .amountFcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).isNotEmpty();
        assertThat(newTx.getViolations().iterator().next().code()).isEqualTo(AMOUNT_LCY_IS_ZERO);
    }

    // Ensuring no violations for items with non-zero LCY amounts.
    @Test
    void whenLcyAndFcyAreNonZero_thenNoViolations() {
        val txId = Transaction.id("2", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountLcy(BigDecimal.valueOf(100))
                                .amountFcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

    // Verifying no violations when both LCY and FCY amounts are zero.
    @Test
    void whenBothLcyAndFcyAreZero_thenNoViolations() {
        val txId = Transaction.id("3", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id("1").build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .amountLcy(BigDecimal.ZERO)
                                .amountFcy(BigDecimal.ZERO)
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

}
