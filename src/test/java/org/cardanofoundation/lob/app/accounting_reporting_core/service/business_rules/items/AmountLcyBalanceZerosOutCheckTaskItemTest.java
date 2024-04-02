package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.LCY_BALANCE_MUST_BE_ZERO;

class AmountLcyBalanceZerosOutCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        PipelineTask pipelineTask = Mockito.mock(PipelineTask.class);
        this.taskItem = new AmountLcyBalanceZerosOutCheckTaskItem(pipelineTask);
    }

    // Checks that no violations are generated when the LCY balances out to zero across transaction items.
    @Test
    void whenLcyBalanceZerosOut_thenNoViolations() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .amountLcy(BigDecimal.valueOf(100))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountLcy(BigDecimal.valueOf(-100))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    // Verifies that a violation is generated when the LCY balance does not zero out.
    @Test
    void whenLcyBalanceDoesNotZeroOut_thenViolationGenerated() {
        val txId = Transaction.id("2", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountLcy(BigDecimal.valueOf(-99))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).isNotEmpty();
        assertThat(newTx.violations().size()).isEqualTo(1);
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(LCY_BALANCE_MUST_BE_ZERO);
    }

    // Confirms that no violations are generated for transactions without any items.
    @Test
    void whenNoTransactionItems_thenNoViolations() {
        val txId = Transaction.id("3", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of())
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    //Tests that a violation is generated when there's only a single side of the transaction, causing the balance not to zero out.
    @Test
    void whenOnlyOneSideOfTransaction_thenViolationGenerated() {
        val txId = Transaction.id("4", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("4")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .amountLcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).isNotEmpty();
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(LCY_BALANCE_MUST_BE_ZERO);
    }

    // Ensures that no violations are generated when multiple items result in an LCY balance that zeros out
    @Test
    void whenLcyBalanceZerosOutWithMultipleItems_thenNoViolations() {
        val txId = Transaction.id("5", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("5")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "3"))
                                .amountLcy(BigDecimal.valueOf(50))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "4"))
                                .amountLcy(BigDecimal.valueOf(30))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "5"))
                                .amountLcy(BigDecimal.valueOf(-80))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

}