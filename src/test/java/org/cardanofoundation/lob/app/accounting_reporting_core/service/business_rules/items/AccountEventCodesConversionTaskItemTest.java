package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationChartOfAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.CHART_OF_ACCOUNT_NOT_FOUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountEventCodesConversionTaskItemTest {

    private PipelineTaskItem taskItem;

    private OrganisationPublicApiIF organisationPublicApiIF;

    @BeforeEach
    public void setup() {
        this.organisationPublicApiIF = mock(OrganisationPublicApiIF.class);
        val pipelineTask = Mockito.mock(PipelineTask.class);
        this.taskItem = new AccountEventCodesConversionTaskItem(
                pipelineTask,
                organisationPublicApiIF
        );
    }

    @Test
    // Chart of Accounts Mapping Found for Both Debit and Credit
    public void testChartOfAccountsMappingFoundForBothDebitAndCredit() {
        val txId = Transaction.id("1", "1");
        val accountDebitRefCode = "DR_REF";
        val accountCreditRefCode = "CR_REF";
        val accountCodeDebit = "1";
        val accountCodeCredit = "2";
        val organisationId = "1";

        when(organisationPublicApiIF.getChartOfAccounts(organisationId, accountCodeCredit))
                .thenReturn(Optional.of(new OrganisationChartOfAccount(new OrganisationChartOfAccount.Id(organisationId, accountCodeCredit), accountCodeCredit, accountCreditRefCode)));

        when(organisationPublicApiIF.getChartOfAccounts(organisationId, accountCodeDebit))
                .thenReturn(Optional.of(new OrganisationChartOfAccount(new OrganisationChartOfAccount.Id(organisationId, accountCodeDebit), accountCodeDebit, accountDebitRefCode)));

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "0"))
                        .accountCodeDebit(Optional.of(accountCodeDebit))
                        .accountCodeCredit(Optional.of(accountCodeCredit))
                        .build()))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
        assertThat(newTx.transaction().getItems()).allMatch(item ->
                item.getAccountEventCode().isPresent() &&
                        item.getAccountEventCode().get().equals(accountDebitRefCode + accountCreditRefCode));
    }

    // Chart of Accounts Mapping Not Found for Debit
    @Test
    public void testChartOfAccountsMappingNotFoundForDebit() {
        val txId = Transaction.id("2", "1");
        val accountCodeDebit = "3";
        val organisationId = "1";

        when(organisationPublicApiIF.getChartOfAccounts(organisationId, accountCodeDebit))
                .thenReturn(Optional.empty());

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.BillCredit)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "1"))
                        .accountCodeDebit(Optional.of(accountCodeDebit))
                        .build()))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(1);
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(CHART_OF_ACCOUNT_NOT_FOUND);
    }


    // Chart of Accounts Mapping Not Found for Credit
    @Test
    public void testChartOfAccountsMappingNotFoundForCredit() {
        val txId = Transaction.id("3", "1");
        val accountCodeCredit = "4";
        val organisationId = "1";

        when(organisationPublicApiIF.getChartOfAccounts(organisationId, accountCodeCredit))
                .thenReturn(Optional.empty());

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("3")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "2"))
                        .accountCodeCredit(Optional.of(accountCodeCredit))
                        .build()))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.transaction().getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.violations()).hasSize(1);
        assertThat(newTx.violations().iterator().next().code()).isEqualTo(CHART_OF_ACCOUNT_NOT_FOUND);
    }

    // No Debit or Credit Account Codes Provided
    @Test
    public void testNoDebitOrCreditAccountCodesProvided() {
        val txId = Transaction.id("4", "1");
        val organisationId = "1";

        val txs = TransactionWithViolations.create(Transaction.builder()
                .id(txId)
                .internalTransactionNumber("4")
                .organisation(Organisation.builder().id(organisationId).build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "3"))
                        .build()))
                .build());

        val newTx = taskItem.run(txs);

        assertThat(newTx.violations()).isEmpty();
    }

}
