package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.PROJECT_CODE_NOT_FOUND;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectConversionTaskItemTest {

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;

    @Mock
    private PipelineTask pipelineTask;

    private ProjectConversionTaskItem projectConversionTaskItem;

    @BeforeEach
    public void setup() {
        this.projectConversionTaskItem = new ProjectConversionTaskItem(pipelineTask, organisationPublicApiIF);
    }

//   Project Found and Conversion Succeeds
//   Test a scenario where the project is successfully found, which should result in a successful conversion without any violations. This tests the positive path and ensures that when everything is as expected, the system behaves correctly.
    @Test
    public void testProjectFoundConversionSucceeds() {
        val txId = Transaction.id("1", "1");
        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "0"))
                        .project(Optional.of(Project.builder().customerCode("cust_code1").build()))
                        .build()))
                .build();

        when(organisationPublicApiIF.findProject("1", "cust_code1"))
                .thenReturn(Optional.of(OrganisationProject.builder()
                        .id(new OrganisationProject.Id("1", "cust_code1"))
                        .build()));

        val newTx = projectConversionTaskItem.run(txs);

        assertThat(newTx.getViolations()).isEmpty();
        // Further assertions can be added here to check the transformed transaction items if needed
    }

    // Missing Project Results in "Conversion Success" but for sure no violations
    @Test
    public void testMissingProjectResultsInConversionSuccess() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "0"))
                        .project(Optional.empty()) // Simulate missing project to trigger the violation
                        .build()))
                .build();

        // Action: Run the task item with the transaction
        val newTx = projectConversionTaskItem.run(txs);

        assertThat(newTx.getViolations()).hasSize(0);
    }

    // Missing Project details in Violation Creation
    @Test
    public void testMissingProjectResultsInViolationCreation() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(TransactionType.FxRevaluation)
                .items(Set.of(TransactionItem.builder()
                        .id(TransactionItem.id(txId, "0"))
                        .project(Optional.of(Project.builder().customerCode("cust_code1").build()))
                        .build()))
                .build();

        when(organisationPublicApiIF.findProject("1", "cust_code1")).thenReturn(Optional.empty());

        // Action: Run the task item with the transaction
        val newTx = projectConversionTaskItem.run(txs);

        assertThat(newTx.getViolations()).isNotEmpty();
        assertThat(newTx.getViolations()).anyMatch(v -> v.code() == PROJECT_CODE_NOT_FOUND);
    }

}
