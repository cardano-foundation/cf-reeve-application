package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.OrganisationCostCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.FxRevaluation;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.COST_CENTER_NOT_FOUND;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostCenterConversionTaskItemTest {

    private PipelineTaskItem taskItem;

    @Mock
    private OrganisationPublicApiIF organisationPublicApiIF;

    @BeforeEach
    public void setup() {
        this.taskItem = new CostCenterConversionTaskItem(organisationPublicApiIF);
    }

    @Test
    // without cost center defined obviously conversion will "succeed"
    void testNoCostCenterConversionSuccess() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(Journal)
                .items(
                        Set.of(
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId, "0"))
                                        .costCenter(Optional.empty())
                                        .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
    }

    @Test
    // without cost center defined obviously conversion will "succeed"
    void testCostCenterConversionSuccess() {
        val txId = Transaction.id("1", "1");

        when(organisationPublicApiIF.findCostCenter("1", "1")).thenReturn(Optional.of(OrganisationCostCenter.builder()
                .id(new OrganisationCostCenter.Id("1", "1"))
                .name("Cost Center 1")
                .externalCustomerCode("2")
                .build()));

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(Journal)
                .items(
                        Set.of(
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId, "0"))
                                        .costCenter(Optional.of(CostCenter.builder().customerCode("1").build()))
                                        .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(VALIDATED);
        assertThat(newTx.getViolations()).isEmpty();
        assertThat(newTx.getItems()).extracting(TransactionItem::getCostCenter).containsExactlyInAnyOrder(Optional.of(CostCenter.builder()
                .customerCode("1")
                .externalCustomerCode(Optional.of("2"))
                .name(Optional.of("Cost Center 1"))
                .build()));
    }

    @Test
    void testRunCostCenterNotFound() {
        val txId = Transaction.id("1", "1");

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(FxRevaluation)
                .items(
                        Set.of(
                                TransactionItem.builder()
                                        .id(TransactionItem.id(txId, "0"))
                                        .costCenter(Optional.of(CostCenter.builder().customerCode("1").build()))
                                        .build()
                        ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).isNotEmpty();
        assertThat(newTx.getViolations()).hasSize(1);
        assertThat(newTx.getViolations().iterator().next().code()).isEqualTo(COST_CENTER_NOT_FOUND);
    }

    @Test
    void testMultipleItemsWithMixedOutcomes() {
        val txId = Transaction.id("2", "1");

        when(organisationPublicApiIF.findCostCenter("1", "1")).thenReturn(Optional.of(OrganisationCostCenter.builder()
                .id(new OrganisationCostCenter.Id("1", "1"))
                .name("Cost Center 1")
                .externalCustomerCode("2")
                .build()));
        when(organisationPublicApiIF.findCostCenter("1", "UNKNOWN")).thenReturn(Optional.empty());

        val txs = Transaction.builder()
                .id(txId)
                .internalTransactionNumber("2")
                .organisation(Organisation.builder().id("1").build())
                .transactionType(Journal)
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "0"))
                                .costCenter(Optional.of(CostCenter.builder().customerCode("1").build()))
                                .build(),
                        TransactionItem.builder()
                                .id(TransactionItem.id(txId, "1"))
                                .costCenter(Optional.of(CostCenter.builder().customerCode("UNKNOWN").build()))
                                .build()
                ))
                .build();

        val newTx = taskItem.run(txs);

        assertThat(newTx.getValidationStatus()).isEqualTo(FAILED);
        assertThat(newTx.getViolations()).hasSize(1);
        assertThat(newTx.getViolations().iterator().next().code()).isEqualTo(COST_CENTER_NOT_FOUND);
    }

}

