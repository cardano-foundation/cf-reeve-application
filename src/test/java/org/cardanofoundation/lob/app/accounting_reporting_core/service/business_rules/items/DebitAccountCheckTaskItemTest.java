package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

public class DebitAccountCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new DebitAccountCheckTaskItem();
    }

    // debit account and credit account are different, so the transaction item is kept
    @Test
    void testRunWithoutCollapsing() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(
                        TransactionItem.builder()
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

        assertThat(newTx.transaction().getItems()).hasSize(2).allMatch(item ->
                !item.getAccountCodeDebit().equals(item.getAccountCodeCredit()));
    }

    // debit account and credit account are the same, so the transaction item is discarded
    @Test
    void testRunWithCollapsing() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(
                        TransactionItem.builder()
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
        assertThat(newTx.transaction().getItems()).extracting(TransactionItem::getId).containsOnly(TransactionItem.id(txId, "1"));
    }

    // will test that we will not collapse failed transactions
    @Test
    void testRunWithCollapsingWithFailedTransactions() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .validationStatus(FAILED)
                .items(Set.of(
                        TransactionItem.builder()
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

        assertThat(newTx.transaction().getItems()).hasSize(2);
        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
    }

    @Test
    public void testMixedValidAndInvalidAccountCodes() {
        val txId = Transaction.id("2", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("3"))
                                .accountCodeDebit(Optional.of("3"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("4"))
                                .accountCodeDebit(Optional.of("5"))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(1);
        assertThat(newTx.transaction().getItems().stream().findFirst().orElseThrow().getAccountCodeDebit()).isEqualTo(Optional.of("5"));
        assertThat(newTx.transaction().getItems().stream().findFirst().orElseThrow().getAccountCodeCredit()).isEqualTo(Optional.of("4"));
    }

    @Test
    public void testTransactionItemsWithMissingAccountCodes() {
        val txId = Transaction.id("3", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.empty())
                                .accountCodeDebit(Optional.of("6"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("7"))
                                .accountCodeDebit(Optional.empty())
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(2);
    }

    @Test
    public void testTransactionsWithNoItems() {
        val txId = Transaction.id("4", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of())
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getItems()).isEmpty();
    }

}
