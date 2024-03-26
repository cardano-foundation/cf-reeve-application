package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DiscardZeroBalanceTxItemsTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new DiscardZeroBalanceTxItemsTaskItem();
    }

    @Test
    // happy path
    public void testNoDiscard() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .amountLcy(BigDecimal.valueOf(0))
                                .amountFcy(BigDecimal.valueOf(100))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountLcy(BigDecimal.valueOf(200))
                                .amountFcy(BigDecimal.valueOf(0))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .amountLcy(BigDecimal.valueOf(300))
                                .amountFcy(BigDecimal.valueOf(300))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(3);
        assertThat(newTx.transaction().getItems().stream().map(TransactionItem::getAmountLcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(0), BigDecimal.valueOf(200), BigDecimal.valueOf(300));
        assertThat(newTx.transaction().getItems().stream().map(TransactionItem::getAmountFcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(0), BigDecimal.valueOf(300));
    }

    @Test
    public void testDiscardTxItemsWithZeroBalance() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .amountLcy(BigDecimal.valueOf(0))
                                .amountFcy(BigDecimal.valueOf(0))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .amountLcy(BigDecimal.valueOf(200))
                                .amountFcy(BigDecimal.valueOf(200))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(1);
        assertThat(newTx.transaction().getItems().stream().map(TransactionItem::getAmountLcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(200));
        assertThat(newTx.transaction().getItems().stream().map(TransactionItem::getAmountFcy)).containsExactlyInAnyOrder(BigDecimal.valueOf(200));
    }

}