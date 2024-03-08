package org.cardanofoundation.lob.app.accounting_reporting_core.config;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

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

        pipelineTasks.add(new PreCleansingPipelineTask());
        pipelineTasks.add(new PreValidationPipelineTask());

        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi, currencyRepository));

        pipelineTasks.add(new PostCleansingPipelineTask());
        pipelineTasks.add(new PostValidationPipelineTask());
        pipelineTasks.add(new LastSanityCheckProcessor(validator));
        pipelineTasks.add(new PostProcessorPipelineTask(transactionRepositoryGateway));

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

}
