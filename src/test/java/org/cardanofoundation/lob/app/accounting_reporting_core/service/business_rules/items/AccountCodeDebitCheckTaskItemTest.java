package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_DEBIT_IS_EMPTY;

class AccountCodeDebitCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AccountCodeDebitCheckTaskItem();
    }

    @Test
    // if we have debit amount then it should be fine
    public void testCreditWorks() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeDebit(Optional.of("1"))
                                .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getViolations()).isEmpty();
    }

    @Test
    // if we don't have debit amount then error should be raised
    public void testAccountDebitCheckError() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).hasSize(1);
        assertThat(newTx.getViolations().iterator().next().code()).isEqualTo(ACCOUNT_CODE_DEBIT_IS_EMPTY);
    }

    @Test
    // we skip transaction type: FxRevaluation from this check
    public void testAccountDebitCheckSkipFxRevaluation() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getViolations()).isEmpty();
    }

    // Multiple Items with Mixed Debit Status
    @Test
    public void testMixedDebitItems() {
        val txId = Transaction.id("2", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeDebit(Optional.of("100"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).hasSize(1);
    }

    // All Items without Debit

    @Test
    public void testAllItemsWithoutDebit() {
        val txId = Transaction.id("3", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "3"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "4"))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).hasSize(2);
    }

    // Debit Code is an Empty String
    @Test
    public void testItemWithEmptyStringDebit() {
        val txId = Transaction.id("4", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("4")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "5"))
                                .accountCodeDebit(Optional.of(""))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).hasSize(1);
    }

    // Transaction with No Items
    @Test
    public void testTransactionWithNoItems() {
        val txId = Transaction.id("5", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("5")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(Collections.emptySet())
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getViolations()).isEmpty();
    }

    // Valid Debit with Whitespace
    @Test
    public void testValidDebitWithWhitespace() {
        val txId = Transaction.id("6", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("6")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "6"))
                                .accountCodeDebit(Optional.of(" 100 "))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getViolations()).isEmpty();
    }

}
