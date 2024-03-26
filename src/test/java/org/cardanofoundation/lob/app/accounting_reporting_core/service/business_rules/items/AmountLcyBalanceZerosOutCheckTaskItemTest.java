package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.LCY_BALANCE_MUST_BE_ZERO;

class AmountLcyBalanceZerosOutCheckTaskItemTest {

    private PipelineTaskItem taskItem;

    @BeforeEach
    public void setup() {
        this.taskItem = new AmountLcyBalanceZerosOutCheckTaskItem((passedOrganisationTransactions, ignoredOrganisationTransactions, allViolationUntilNow) -> null);
    }

    @Test
        // happy path, no violations
    void testBalanceZero() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(
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

    @Test
    void testBalanceIsNotZero() {
        val txId = Transaction.id("1", "1");

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId, "1"))
                                        .amountLcy(BigDecimal.valueOf(-100))
                                        .build()
                        ))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isNotEmpty();
        assertThat(newTx.violations().size()).isEqualTo(1);
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(LCY_BALANCE_MUST_BE_ZERO);
    }

}