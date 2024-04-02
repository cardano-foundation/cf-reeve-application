package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.*;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultBusinessRulesPipelineProcessorTest {

    @Mock
    private PipelineTask mockTask1, mockTask2;

    private BusinessRulesPipelineProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new DefaultBusinessRulesPipelineProcessor(Arrays.asList(mockTask1, mockTask2));
    }

    // No Transformations and No Violations
    @Test
    void givenNoTransformationsAndViolations_whenProcessed_thenRemainUnchanged() {
        val originalTransactions = new OrganisationTransactions("org1", Set.of());
        val ignoredTransactions = new OrganisationTransactions("org1", Set.of());
        val initialViolations = new HashSet<Violation>();

        when(mockTask1.run(any(), any(), any())).thenReturn(new TransformationResult(originalTransactions, ignoredTransactions, Set.of()));
        when(mockTask2.run(any(), any(), any())).thenReturn(new TransformationResult(originalTransactions, ignoredTransactions, Set.of()));

        val result = processor.run(originalTransactions, ignoredTransactions, initialViolations);

        assertThat(result.passedTransactions()).isEqualTo(originalTransactions);
        assertThat(result.ignoredTransactions()).isEqualTo(ignoredTransactions);
        assertThat(result.violations()).isEmpty();
    }

    //  Adds Violations
    @Test
    void givenTaskAddsViolations_whenProcessed_thenAggregateViolations() {
        val originalTransactions = new OrganisationTransactions("org1", Set.of());
        val ignoredTransactions = new OrganisationTransactions("org1", Set.of());
        val initialViolations = new HashSet<Violation>();
        val violation = new Violation(Violation.Type.ERROR, INTERNAL, "org1", "tx1", Optional.empty(), TX_SANITY_CHECK_FAIL, "MockTask", Map.of());

        when(mockTask1.run(any(), any(), any())).thenReturn(new TransformationResult(originalTransactions, ignoredTransactions, Set.of(violation)));
        when(mockTask2.run(any(), any(), any())).thenReturn(new TransformationResult(originalTransactions, ignoredTransactions, Set.of()));

        val result = processor.run(originalTransactions, ignoredTransactions, initialViolations);

        assertThat(result.violations()).containsExactly(violation);
    }

    // Transform Transactions
    @Test
    void givenTasksTransformTransactions_whenProcessed_thenUpdatePassedTransactions() {
        val orgId = "org1";
        val tx1Id = Transaction.id(orgId, "1");

        val tx1 = Transaction.builder()
                .id(tx1Id)
                .internalTransactionNumber("1")
                .organisation(Organisation.builder().id(orgId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(tx1Id, "0"))
                                .amountLcy(BigDecimal.ZERO)
                                .amountFcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val originalTransactions = new OrganisationTransactions(orgId, Set.of());
        val transformedTransactions = new OrganisationTransactions(orgId, Set.of(tx1));
        val ignoredTransactions = new OrganisationTransactions(orgId, Set.of());
        val initialViolations = new HashSet<Violation>();

        when(mockTask1.run(any(), any(), any())).thenReturn(new TransformationResult(transformedTransactions, ignoredTransactions, Set.of()));
        when(mockTask2.run(any(), any(), any())).thenReturn(new TransformationResult(transformedTransactions, ignoredTransactions, Set.of()));

        val result = processor.run(originalTransactions, ignoredTransactions, initialViolations);

        assertThat(result.passedTransactions().transactions()).containsExactly(tx1);
    }

    // Mixed Violations and Transformations
    @Test
    void givenTasksAddViolationsAndTransform_whenProcessed_thenUpdateTransactionsAndViolations() {
        val orgId = "org1";
        val tx3Id = Transaction.id(orgId, "tx3");

        val tx3 = Transaction.builder()
                .id(tx3Id)
                .internalTransactionNumber("tx3")
                .organisation(Organisation.builder().id(orgId).build())
                .items(Set.of(
                        TransactionItem.builder()
                                .id(TransactionItem.id(tx3Id, "0"))
                                .amountLcy(BigDecimal.ZERO)
                                .amountFcy(BigDecimal.valueOf(100))
                                .build()
                ))
                .build();

        val originalTransactions = new OrganisationTransactions(orgId, Set.of());
        val transformedTransactions = new OrganisationTransactions(orgId, Set.of(tx3));
        val ignoredTransactions = new OrganisationTransactions(orgId, Set.of());
        val initialViolations = new HashSet<Violation>();
        val violationFromTask1 = new Violation(Violation.Type.WARN, LOB, orgId, "tx1", Optional.empty(), DOCUMENT_MUST_BE_PRESENT, "MockTask1", Map.of());
        val violationFromTask2 = new Violation(Violation.Type.ERROR, ERP, orgId, "tx2", Optional.empty(), ACCOUNT_CODE_CREDIT_IS_EMPTY, "MockTask2", Map.of());

        when(mockTask1.run(any(), any(), any())).thenReturn(new TransformationResult(transformedTransactions, ignoredTransactions, Set.of(violationFromTask1)));
        when(mockTask2.run(any(), any(), any())).thenReturn(new TransformationResult(transformedTransactions, ignoredTransactions, Set.of(violationFromTask2)));

        val result = processor.run(originalTransactions, ignoredTransactions, initialViolations);

        assertThat(result.passedTransactions().transactions()).containsExactly(tx3);
        assertThat(result.violations()).containsExactlyInAnyOrder(violationFromTask1, violationFromTask2);
    }

}
