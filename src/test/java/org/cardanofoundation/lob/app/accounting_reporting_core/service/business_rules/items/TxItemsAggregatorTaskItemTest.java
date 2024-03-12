package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TxItemsAggregatorTaskItemTest {

    private TxItemsAggregatorTaskItem txItemsAggregatorTaskItem;

    @BeforeEach
    public void setup() {
        this.txItemsAggregatorTaskItem = new TxItemsAggregatorTaskItem();
    }

    @Test
    void shouldNotCollapseItems() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .accountCodeEventRefCredit(Optional.of("r1"))
                                .accountCodeEventRefDebit(Optional.of("r2"))
                                .accountEventCode(Optional.of("e12"))
                                .amountLcy(BigDecimal.ONE)
                                .amountFcy(BigDecimal.TEN)
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("12"))
                                .accountCodeDebit(Optional.of("22"))
                                .accountCodeEventRefCredit(Optional.of("r12"))
                                .accountCodeEventRefDebit(Optional.of("r22"))
                                .accountEventCode(Optional.of("e1212"))
                                .amountLcy(BigDecimal.ONE)
                                .amountFcy(BigDecimal.TEN)
                                .build()
                ))
                .build());

        val newTx = txItemsAggregatorTaskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(2);
        assertThat(newTx.transaction().getItems()).extracting("accountCodeCredit").containsExactlyInAnyOrder(Optional.of("1"), Optional.of("12"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeDebit").containsExactlyInAnyOrder(Optional.of("2"), Optional.of("22"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeEventRefCredit").containsExactlyInAnyOrder(Optional.of("r1"), Optional.of("r12"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeEventRefDebit").containsExactlyInAnyOrder(Optional.of("r2"), Optional.of("r22"));
        assertThat(newTx.transaction().getItems()).extracting("accountEventCode").containsExactlyInAnyOrder(Optional.of("e12"), Optional.of("e1212"));
    }

    @Test
    void shouldCollapseItems() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .accountCodeEventRefCredit(Optional.of("r1"))
                                .accountCodeEventRefDebit(Optional.of("r2"))
                                .accountEventCode(Optional.of("e12"))
                                .amountLcy(BigDecimal.ONE)
                                .amountFcy(BigDecimal.TEN)
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("1"))
                                .accountCodeDebit(Optional.of("2"))
                                .accountCodeEventRefCredit(Optional.of("r1"))
                                .accountCodeEventRefDebit(Optional.of("r2"))
                                .accountEventCode(Optional.of("e12"))
                                .amountLcy(BigDecimal.ONE)
                                .amountFcy(BigDecimal.TEN)
                                .build()
                ))
                .build());

        val newTx = txItemsAggregatorTaskItem.run(txs);

        assertThat(newTx.transaction().getItems()).hasSize(1);
        assertThat(newTx.transaction().getItems()).extracting("amountLcy").containsExactly(BigDecimal.valueOf(2));
        assertThat(newTx.transaction().getItems()).extracting("amountFcy").containsExactly(BigDecimal.valueOf(20));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeCredit").containsExactly(Optional.of("1"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeDebit").containsExactly(Optional.of("2"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeEventRefCredit").containsExactly(Optional.of("r1"));
        assertThat(newTx.transaction().getItems()).extracting("accountCodeEventRefDebit").containsExactly(Optional.of("r2"));
        assertThat(newTx.transaction().getItems()).extracting("accountEventCode").containsExactly(Optional.of("e12"));
    }

    @Test
    void shouldCollapseSomeItemsAndNotOthers() {
        val txId1 = Transaction.id("1", "1");
        val txId2 = Transaction.id("1", "2");
        val txId3 = Transaction.id("1", "3");

        val txs = Set.of(
                TransactionWithViolations.create(Transaction.builder()
                        .id(txId1)
                        .items(Set.of(TransactionItem.builder()
                                        .id(TransactionItem.id(txId1, "0"))
                                        .accountCodeCredit(Optional.of("1"))
                                        .accountCodeDebit(Optional.of("2"))
                                        .accountCodeEventRefCredit(Optional.of("r1"))
                                        .accountCodeEventRefDebit(Optional.of("r2"))
                                        .accountEventCode(Optional.of("e12"))
                                        .amountLcy(BigDecimal.ONE)
                                        .amountFcy(BigDecimal.TEN)
                                        .build(),
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId1, "1"))
                                        .accountCodeCredit(Optional.of("1"))
                                        .accountCodeDebit(Optional.of("2"))
                                        .accountCodeEventRefCredit(Optional.of("r1"))
                                        .accountCodeEventRefDebit(Optional.of("r2"))
                                        .accountEventCode(Optional.of("e12"))
                                        .amountLcy(BigDecimal.ONE)
                                        .amountFcy(BigDecimal.TEN)
                                        .build()
                        ))
                        .build()),
                TransactionWithViolations.create(Transaction.builder()
                        .id(txId2)
                        .items(Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId2, "0"))
                                .accountCodeCredit(Optional.of("3"))
                                .accountCodeDebit(Optional.of("4"))
                                .accountCodeEventRefCredit(Optional.of("r3"))
                                .accountCodeEventRefDebit(Optional.of("r4"))
                                .accountEventCode(Optional.of("e34"))
                                .amountLcy(BigDecimal.ONE)
                                .amountFcy(BigDecimal.TEN)
                                .build()
                        ))
                        .build(), Set.of()),
                TransactionWithViolations.create(Transaction.builder()
                        .id(txId3)
                        .items(Set.of(TransactionItem.builder()
                                        .id(TransactionItem.id(txId3, "0"))
                                        .accountCodeCredit(Optional.of("5"))
                                        .accountCodeDebit(Optional.of("6"))
                                        .accountCodeEventRefCredit(Optional.of("r5"))
                                        .accountCodeEventRefDebit(Optional.of("r6"))
                                        .accountEventCode(Optional.of("e56"))
                                        .amountLcy(BigDecimal.ONE)
                                        .amountFcy(BigDecimal.TEN)
                                        .build(),
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId3, "1"))
                                        .accountCodeCredit(Optional.of("5"))
                                        .accountCodeDebit(Optional.of("6"))
                                        .accountCodeEventRefCredit(Optional.of("r5"))
                                        .accountCodeEventRefDebit(Optional.of("r6"))
                                        .accountEventCode(Optional.of("e56"))
                                        .amountLcy(BigDecimal.ONE)
                                        .amountFcy(BigDecimal.TEN)
                                        .build(),
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId3, "1"))
                                        .accountCodeCredit(Optional.of("10"))
                                        .accountCodeDebit(Optional.of("11"))
                                        .accountCodeEventRefCredit(Optional.of("r51"))
                                        .accountCodeEventRefDebit(Optional.of("r61"))
                                        .accountEventCode(Optional.of("e56111"))
                                        .amountLcy(BigDecimal.TWO)
                                        .amountFcy(BigDecimal.TEN)
                                        .build()
                        ))
                        .build(), Set.of())
        );

        val newTxs = txs.stream().map(txItemsAggregatorTaskItem::run).collect(Collectors.toCollection( LinkedHashSet::new ));

        assertThat(newTxs).hasSize(3);
        assertThat(newTxs).extracting("transaction.id").containsExactlyInAnyOrder(txId1, txId2, txId3);

        assertThat(newTxs.stream().filter(tx -> tx.transaction().getId().equals(txId1)).findFirst().orElseThrow().transaction().getItems()).hasSize(1);
        assertThat(newTxs.stream().filter(tx -> tx.transaction().getId().equals(txId2)).findFirst().orElseThrow().transaction().getItems()).hasSize(1);
        assertThat(newTxs.stream().filter(tx -> tx.transaction().getId().equals(txId3)).findFirst().orElseThrow().transaction().getItems()).hasSize(2);

        assertThat(newTxs.stream().filter(tx -> tx.transaction().getId().equals(txId3)).findFirst().orElseThrow().transaction().getItems()).extracting("amountLcy").containsExactlyInAnyOrder(BigDecimal.valueOf(2), BigDecimal.valueOf(2));
        assertThat(newTxs.stream().filter(tx -> tx.transaction().getId().equals(txId3)).findFirst().orElseThrow().transaction().getItems()).extracting("amountFcy").containsExactlyInAnyOrder(BigDecimal.valueOf(10), BigDecimal.valueOf(20));
    }

}
