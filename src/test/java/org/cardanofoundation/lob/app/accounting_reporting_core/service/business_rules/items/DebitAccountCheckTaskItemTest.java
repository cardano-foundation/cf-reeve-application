package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

public class DebitAccountCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new DebitAccountCheckTaskItem();
    }

    @Test
    void testRunWithoutCollapsing() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAccountCodeCredit("1");
        txItem1.setAccountCodeDebit("2");

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAccountCodeCredit("1");
        txItem2.setAccountCodeDebit("2");

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(2).allMatch(item ->
                !item.getAccountCodeDebit().equals(item.getAccountCodeCredit()));
    }

    @Test
    void testRunWithCollapsing() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAccountCodeCredit("1");
        txItem1.setAccountCodeDebit("1");

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAccountCodeCredit("1");
        txItem2.setAccountCodeDebit("2");

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(1);
        assertThat(tx.getItems()).extracting(TransactionItemEntity::getId).containsOnly(TransactionItem.id(txId, "1"));
    }

    @Test
    void testRunWithCollapsingWithFailedTransactions() {
        val txId = Transaction.id("1", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAccountCodeCredit("1");
        txItem1.setAccountCodeDebit("1");

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAccountCodeCredit("1");
        txItem2.setAccountCodeDebit("2");

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setValidationStatus(FAILED);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(2);
        assertThat(tx.getValidationStatus()).isEqualTo(FAILED);
    }

    @Test
    public void testMixedValidAndInvalidAccountCodes() {
        val txId = Transaction.id("2", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.setAccountCodeCredit("3");
        txItem1.setAccountCodeDebit("3");

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAccountCodeCredit("4");
        txItem2.setAccountCodeDebit("5");

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(1);
        assertThat(tx.getItems().stream().findFirst().orElseThrow().getAccountCodeDebit().orElseThrow()).isEqualTo("5");
        assertThat(tx.getItems().stream().findFirst().orElseThrow().getAccountCodeCredit().orElseThrow()).isEqualTo("4");
    }

    @Test
    public void testTransactionItemsWithMissingAccountCodes() {
        val txId = Transaction.id("3", "1");

        val txItem1 = new TransactionItemEntity();
        txItem1.setId(TransactionItem.id(txId, "0"));
        txItem1.clearAccountCodeCredit();
        txItem1.setAccountCodeDebit("6");

        val txItem2 = new TransactionItemEntity();
        txItem2.setId(TransactionItem.id(txId, "1"));
        txItem2.setAccountCodeCredit("7");
        txItem2.clearAccountCodeDebit();

        val txItems = new LinkedHashSet<TransactionItemEntity>();
        txItems.add(txItem1);
        txItems.add(txItem2);

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(txItems);

        taskItem.run(tx);

        assertThat(tx.getItems()).hasSize(2);
    }

    @Test
    public void testTransactionsWithNoItems() {
        val txId = Transaction.id("4", "1");

        val tx = new TransactionEntity();
        tx.setId(txId);
        tx.setItems(new LinkedHashSet<>());

        taskItem.run(tx);

        assertThat(tx.getItems()).isEmpty();
    }

}
