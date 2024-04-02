package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.FCY_BALANCE_MUST_BE_ZERO;

class AmountFcyBalanceZerosOutCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        val pipelineTask = Mockito.mock(PipelineTask.class);
        this.taskItem = new AmountFcyBalanceZerosOutCheckTaskItem(pipelineTask);
    }

    @Test
    // Validates that no violations are generated when the sum of all FCY amounts in transaction items equals zero.
    public void whenFcyBalanceZerosOut_thenNoViolations() {
        var txId = Transaction.id("1", "1");
        var organisationId = "1";

        var txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(txId, "0")).amountFcy(new BigDecimal("100")).build(),
                        TransactionItem.builder().id(TransactionItem.id(txId, "1")).amountFcy(new BigDecimal("-100")).build()
                ))
                .build());

        var newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    @Test
    // whenFcyBalanceDoesNotZeroOut_thenViolationGenerated
    public void whenFcyBalanceDoesNotZeroOut_thenViolationGenerated() {
        var txId = Transaction.id("2", "1");
        var organisationId = "1";

        var txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder().id(TransactionItem.id(txId, "0")).amountFcy(new BigDecimal("100")).build(),
                        TransactionItem.builder().id(TransactionItem.id(txId, "1")).amountFcy(new BigDecimal("-90")).build()
                ))
                .build());

        var newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isNotEmpty();
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(FCY_BALANCE_MUST_BE_ZERO);
    }

    @Test
    // whenNoTransactionItems_thenNoViolations
    public void whenNoTransactionItems_thenNoViolations() {
        var txId = Transaction.id("3", "1");
        var organisationId = "1";

        var txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of())
                .build());

        var newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

}
