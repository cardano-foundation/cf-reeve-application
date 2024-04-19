package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBusinessRulesPipelineProcessorTest {

    @Mock
    private PipelineTask pipelineTask;

    @InjectMocks
    private DefaultBusinessRulesPipelineProcessor processor;

    private OrganisationTransactions initialOrgTransactions;
    private OrganisationTransactions initialIgnoredTransactions;

    @BeforeEach
    void setUp() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setValidationStatus(ValidationStatus.VALIDATED);
        initialOrgTransactions = new OrganisationTransactions("org1", new HashSet<>(Set.of(transactionEntity)));
        initialIgnoredTransactions = new OrganisationTransactions("org1", new HashSet<>());
    }

    @Test
    void run_NoTasks_ShouldReturnInitialTransactionsUnchanged() {
        processor = new DefaultBusinessRulesPipelineProcessor(Collections.emptyList());

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(result.passedTransactions()).isEqualTo(initialOrgTransactions);
        assertThat(result.ignoredTransactions()).isEqualTo(initialIgnoredTransactions);
    }

    @Test
    void run_SingleTask_ShouldTransformTransactions() {
        TransactionEntity transformedTransaction = new TransactionEntity();
        transformedTransaction.setValidationStatus(ValidationStatus.VALIDATED);
        OrganisationTransactions transformedOrgTransactions = new OrganisationTransactions("org1", Set.of(transformedTransaction));
        OrganisationTransactions ignoredTransactions = new OrganisationTransactions("org1", Set.of());

        when(pipelineTask.run(any(), any())).thenReturn(new TransformationResult(transformedOrgTransactions, ignoredTransactions));

        processor = new DefaultBusinessRulesPipelineProcessor(List.of(pipelineTask));

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(result.passedTransactions()).isEqualTo(transformedOrgTransactions);
        assertThat(result.ignoredTransactions()).isEqualTo(ignoredTransactions);
    }

    @Test
    void run_MultipleTasks_ShouldAccumulateTransformations() {
        TransactionEntity firstTransformedTransaction = new TransactionEntity();
        firstTransformedTransaction.setValidationStatus(ValidationStatus.VALIDATED);
        OrganisationTransactions firstTransformedOrgTransactions = new OrganisationTransactions("org1", Set.of(firstTransformedTransaction));

        TransactionEntity secondTransformedTransaction = new TransactionEntity();
        secondTransformedTransaction.setValidationStatus(ValidationStatus.VALIDATED);
        OrganisationTransactions secondTransformedOrgTransactions = new OrganisationTransactions("org1", Set.of(secondTransformedTransaction));

        OrganisationTransactions firstIgnoredTransactions = new OrganisationTransactions("org1", Set.of());
        OrganisationTransactions secondIgnoredTransactions = new OrganisationTransactions("org1", Set.of());

        val firstTask = mock(PipelineTask.class);
        val secondTask = mock(PipelineTask.class);

        when(firstTask.run(any(), any())).thenReturn(new TransformationResult(firstTransformedOrgTransactions, firstIgnoredTransactions));
        when(secondTask.run(any(), any())).thenReturn(new TransformationResult(secondTransformedOrgTransactions, secondIgnoredTransactions));

        processor = new DefaultBusinessRulesPipelineProcessor(List.of(firstTask, secondTask));

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(result.passedTransactions()).isEqualTo(secondTransformedOrgTransactions);
        assertThat(result.ignoredTransactions()).isEqualTo(secondIgnoredTransactions);
    }

}
