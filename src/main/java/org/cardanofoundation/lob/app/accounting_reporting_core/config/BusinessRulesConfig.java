package org.cardanofoundation.lob.app.accounting_reporting_core.config;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.DefaultPipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DbSyncProcessorPipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DefaultBusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BusinessRulesConfig {

    private final Validator validator;
    private final TransactionRepositoryGateway transactionRepositoryGateway;
    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository currencyRepository;

    @Bean
    public BusinessRulesPipelineProcessor businessRulesPipelineProcessor() {
        val pipelineTasks = new ArrayList<PipelineTask>();

        pipelineTasks.add(sanityCheckPipelineTask());

        pipelineTasks.add(preCleansingPipelineTask());
        pipelineTasks.add(preValidationPipelineTask());

        pipelineTasks.add(conversionPipelineTask());

        pipelineTasks.add(postCleansingPipelineTask());
        pipelineTasks.add(postValidationPipelineTask());
        pipelineTasks.add(new DbSyncProcessorPipelineTask(transactionRepositoryGateway));

        pipelineTasks.add(sanityCheckPipelineTask());

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

    private PipelineTask sanityCheckPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.<PipelineTaskItem>of(
                new SanityCheckFieldsTaskItem(validator)
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

    private PipelineTask preCleansingPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.<PipelineTaskItem>of(
                new DiscardZeroBalanceTxItemsTaskItem()
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

    private PipelineTask preValidationPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.of(
                new AmountsFcyCheckTaskItem(pipelineTask),
                new AmountsLcyCheckTaskItem(pipelineTask),
                new AmountLcyBalanceZerosOutCheckTaskItem(pipelineTask),
                new AmountFcyBalanceZerosOutCheckTaskItem(pipelineTask)
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

    private PipelineTask conversionPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.of(
                new OrganisationConversionTaskItem(pipelineTask, organisationPublicApi, currencyRepository),
                new DocumentConversionTaskItem(pipelineTask, organisationPublicApi, currencyRepository),
                new CostCenterConversionTaskItem(pipelineTask, organisationPublicApi),
                new AccountEventCodesConversionTaskItem(pipelineTask, organisationPublicApi)
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

    private PipelineTask postCleansingPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.of(
                new DebitAccountCheckTaskItem(),
                new TxItemsCollapsingTaskItem()
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

    private PipelineTask postValidationPipelineTask() {
        val pipelineTask = new DefaultPipelineTask();

        val steps = List.of(
                new AccountCodeDebitCheckTaskItem(pipelineTask),
                new AccountCodeCreditCheckTaskItem(pipelineTask),
                new DocumentMustBePresentTaskItem(pipelineTask),
                new NoTransactionItemsTaskItem(pipelineTask)
        );

        pipelineTask.setItems(steps);

        return pipelineTask;
    }

}

