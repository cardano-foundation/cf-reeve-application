package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DebitAccountCheckTaskItemTest {

    private DebitAccountCheckTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new DebitAccountCheckTaskItem();
    }

    @Test
    // debit account and credit account are different, so the transaction item is kept
    public void testRunWithoutCollapsing() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(2);
    }

    @Test
    // debit account and credit account are the same, so the transaction item is discarded
    public void testRunWithCollapsing() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("1"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(1);
        assertThat(newTx.transaction().getItems().stream().findFirst().orElseThrow().getId()).isEqualTo(TransactionItem.id(txId, "1"));
    }

}
