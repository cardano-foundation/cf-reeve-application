package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Project;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectConversionTaskItemTest {

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;

    private ProjectConversionTaskItem projectConversionTaskItem;

    @BeforeEach
    public void setup() {
        this.projectConversionTaskItem = new ProjectConversionTaskItem(organisationPublicApiIF);
    }

    @Test
    public void testProjectFoundConversionSucceeds() {
        val project = Project.builder().customerCode("cust_code1").build();

        val txItem = new TransactionItemEntity();
        txItem.setId("1:0");
        txItem.setProject(project);

        Set<TransactionItemEntity> items = new HashSet<>();
        items.add(txItem);

        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setTransactionInternalNumber("1");
        transaction.setOrganisation(Organisation.builder()
                .id("1")
                .build());
        transaction.setItems(items);

        when(organisationPublicApiIF.findProject("1", "cust_code1"))
                .thenReturn(Optional.of(new OrganisationProject(new OrganisationProject.Id("1", "cust_code1"))));

        projectConversionTaskItem.run(transaction);

        assertThat(transaction.getViolations()).isEmpty();
    }

    @Test
    public void testMissingProjectResultsInConversionSuccess() {
        val txItem = new TransactionItemEntity();
        txItem.setId("1:0");
        txItem.setProject(null);

        Set<TransactionItemEntity> items = new HashSet<>();
        items.add(txItem);

        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setTransactionInternalNumber("1");
        transaction.setOrganisation(Organisation.builder()
                .id("1")
                .build());
        transaction.setItems(items);

        projectConversionTaskItem.run(transaction);

        assertThat(transaction.getViolations()).isEmpty();
    }

    @Test
    public void testMissingProjectResultsInViolationCreation() {
        val project = Project.builder().customerCode("cust_code1").build();

        val txItem = new TransactionItemEntity();
        txItem.setId("1:0");
        txItem.setProject(project);

        Set<TransactionItemEntity> items = new HashSet<>();
        items.add(txItem);

        val transaction = new TransactionEntity();
        transaction.setId("1");
        transaction.setTransactionInternalNumber("1");
        transaction.setOrganisation(Organisation.builder()
                .id("1")
                .build());
        transaction.setItems(items);

        when(organisationPublicApiIF.findProject("1", "cust_code1")).thenReturn(Optional.empty());

        projectConversionTaskItem.run(transaction);

        assertThat(transaction.getViolations()).isNotEmpty();
        assertThat(transaction.getViolations()).anyMatch(v -> v.getCode() == Violation.Code.PROJECT_DATA_NOT_FOUND);
    }

}
