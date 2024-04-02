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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.ACCOUNT_CODE_CREDIT_IS_EMPTY;

public class AccountCodeCreditCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        val pipelineTask = Mockito.mock(PipelineTask.class);
        this.taskItem = new AccountCodeCreditCheckTaskItem(pipelineTask);
    }

    @Test
    // If we have credit amount then it should be fine
    public void testCreditWorks() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .accountCodeCredit(Optional.of("1"))
                                .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    @Test
    // If we don't have credit amount then error should be raised
    public void testAccountCreditCheckError() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(1);
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(ACCOUNT_CODE_CREDIT_IS_EMPTY);
    }

    @Test
    // We skip transaction type: JOURNAL from this check
    public void testAccountCreditCheckSkipJournals() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(Journal)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    // Multiple Items with Mixed Credit Status
    @Test
    public void testMixedCreditItems() {
        val txId = Transaction.id("2", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .accountCodeCredit(Optional.of("100"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "2"))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(1);
    }

    // Multiple Items, All Without Credit
    @Test
    public void testAllItemsWithoutCredit() {
        val txId = Transaction.id("3", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "3"))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "4"))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(2);
    }

    // Item with Empty String as Credit
    @Test
    public void testItemWithEmptyStringCredit() {
        val txId = Transaction.id("4", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("4")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "5"))
                                .accountCodeCredit(Optional.of(""))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(1);
    }

    // Transaction with No Items
    @Test
    public void testTransactionWithNoItems() {
        val txId = Transaction.id("5", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("5")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Collections.emptySet())
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    // Valid Credit with Whitespace
    @Test
    public void testValidCreditWithWhitespace() {
        val txId = Transaction.id("6", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("6")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "6"))
                                .accountCodeCredit(Optional.of(" 100 "))
                                .build()
                ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

}
