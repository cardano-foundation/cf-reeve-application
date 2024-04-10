package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        initialOrgTransactions = new OrganisationTransactions("org1", Set.of());
        initialIgnoredTransactions = new OrganisationTransactions("org1", Set.of());
    }

    @Test
    void run_NoTasks_ShouldReturnInitialTransactionsUnchanged() {
        processor = new DefaultBusinessRulesPipelineProcessor(Collections.emptyList());

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(initialOrgTransactions).isEqualTo(result.passedTransactions());
        assertThat(initialIgnoredTransactions).isEqualTo(result.ignoredTransactions());
    }

    @Test
    void run_SingleTask_ShouldTransformTransactions() {
        OrganisationTransactions transformedOrgTransactions =  new OrganisationTransactions("org1", Set.of());
        OrganisationTransactions ignoredTransactions =  new OrganisationTransactions("org1", Set.of());
        when(pipelineTask.run(any(), any())).thenReturn(new TransformationResult(transformedOrgTransactions, ignoredTransactions));

        processor = new DefaultBusinessRulesPipelineProcessor(List.of(pipelineTask));

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(transformedOrgTransactions).isEqualTo(result.passedTransactions());
        assertThat(ignoredTransactions).isEqualTo(result.ignoredTransactions());
    }

    @Test
    void run_MultipleTasks_ShouldAccumulateTransformations() {
        OrganisationTransactions firstTransformedOrgTransactions = new OrganisationTransactions("org1", Set.of());
        OrganisationTransactions secondTransformedOrgTransactions = new OrganisationTransactions("org1", Set.of());
        OrganisationTransactions firstIgnoredTransactions = new OrganisationTransactions("org1", Set.of());
        OrganisationTransactions secondIgnoredTransactions = new OrganisationTransactions("org1", Set.of());

        val firstTask = mock(PipelineTask.class);
        val secondTask = mock(PipelineTask.class);

        when(firstTask.run(any(), any())).thenReturn(new TransformationResult(firstTransformedOrgTransactions, firstIgnoredTransactions));
        when(secondTask.run(any(), any())).thenReturn(new TransformationResult(secondTransformedOrgTransactions, secondIgnoredTransactions));

        processor = new DefaultBusinessRulesPipelineProcessor(List.of(firstTask, secondTask));

        val result = processor.run(initialOrgTransactions, initialIgnoredTransactions);

        assertThat(secondTransformedOrgTransactions).isEqualTo(result.passedTransactions());
        assertThat(secondIgnoredTransactions).isEqualTo(result.ignoredTransactions());
    }

}
