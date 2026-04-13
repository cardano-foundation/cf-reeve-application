package org.cardanofoundation.lob.app.autoconfig;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
        name = "lob.accounting_reporting_core.rules.cardano_rules",
        havingValue = "false"
)
public class CustomBusinessRulesConfig {

    private final Validator validator;
    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository currencyRepository;

    public CustomBusinessRulesConfig(Validator validator,
                                     OrganisationPublicApi organisationPublicApi,
                                     CoreCurrencyRepository currencyRepository) {
        this.validator = validator;
        this.organisationPublicApi = organisationPublicApi;
        this.currencyRepository = currencyRepository;
        log.info("\n\n###########################################################################\nCustomBusinessRulesConfig created - Cardano rules DISABLED\n");
    }

    @Bean
    @Qualifier("selectorBusinessRulesProcessors")
    public BusinessRulesPipelineProcessor selectorBusinessRulesProcessors() {
        return new BusinessRulesPipelineSelector(
                defaultBusinessRulesProcessor(),
                reprocessBusinessRulesProcessor()
        );
    }

    @Bean
    @Qualifier("defaultBusinessRulesProcessor")
    public BusinessRulesPipelineProcessor defaultBusinessRulesProcessor() {
        log.info("\n\n###########################################################################\nCardano rules DISABLED - building custom defaultBusinessRulesProcessor\n");

        val pipelineTasks = new ArrayList<PipelineTask>();

        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new SanityCheckFieldsTaskItem(validator),
                new TransactionTypeUnknownTaskItem()
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new DiscardZeroBalanceTxItemsTaskItem()
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new AmountsFcyCheckTaskItem(),
                new AmountsLcyCheckTaskItem(),
                new JournalAccountCreditEnrichmentTaskItem(organisationPublicApi)
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new OrganisationConversionTaskItem(organisationPublicApi, currencyRepository),
                new DocumentConversionTaskItem(organisationPublicApi, currencyRepository),
                new CostCenterConversionTaskItem(organisationPublicApi),
                new ProjectConversionTaskItem(organisationPublicApi),
                new AccountEventCodesConversionTaskItem(organisationPublicApi)
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new DiscardSameAccountCodeTaskItem(),
                new TxItemsAmountsSummingTaskItem(),
                new AmountsLcyAfterSummingCheckTaskItem()
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new AccountCodeDebitCheckTaskItem(),
                new AccountCodeCreditCheckTaskItem(),
                new DocumentMustBePresentTaskItem(),
                new CheckIfAllTxItemsAreErasedTaskItem(),
                new NetOffCreditDebitTaskItem(organisationPublicApi)
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new SanityCheckFieldsTaskItem(validator),
                new TransactionTypeUnknownTaskItem()
        )));

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

    @Bean
    @Qualifier("reprocessBusinessRulesProcessor")
    public BusinessRulesPipelineProcessor reprocessBusinessRulesProcessor() {
        val pipelineTasks = new ArrayList<PipelineTask>();

        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new OrganisationConversionTaskItem(organisationPublicApi, currencyRepository),
                new DocumentConversionTaskItem(organisationPublicApi, currencyRepository),
                new CostCenterConversionTaskItem(organisationPublicApi),
                new ProjectConversionTaskItem(organisationPublicApi),
                new AccountEventCodesConversionTaskItem(organisationPublicApi)
        )));
        pipelineTasks.add(new DefaultPipelineTask(List.of(
                new DiscardSameAccountCodeTaskItem(),
                new TxItemsAmountsSummingTaskItem(),
                new AmountsLcyAfterSummingCheckTaskItem()
        )));

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

}
