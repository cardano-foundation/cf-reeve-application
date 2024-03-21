package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;

class AccountCodeDebitCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AccountCodeDebitCheckTaskItem((passedOrganisationTransactions, ignoredOrganisationTransactions, allViolationUntilNow) -> null);
    }

    @Test
    // if we have debit amount then it should be fine
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
                                .accountCodeDebit(Optional.of("1"))
                                .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);


        assertThat(newTx.violations()).isEmpty();
    }

    @Test
    // if we have debit amount then it should be fine
    public void testAllOk() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
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
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

    @Test
    // if we have debit amount then it should be fine
    public void testAccountDebitCheckError() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.BillCredit)
                .items(
                        Set.of(TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).hasSize(1);
    }

    @Test
    // we skip transaction type: FxRevaluation from this check
    public void testAccountDebitCheckSkipFxRevaluation() {
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


        assertThat(newTx.violations()).isEmpty();
    }

}