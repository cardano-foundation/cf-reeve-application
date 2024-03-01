package org.cardanofoundation.lob.app.netsuite_adapter.config;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class BusinessRulesConfig {

    private final Validator validator;

    @Bean
    public BusinessRulesPipelineProcessor businessRulesPipelineProcessor(OrganisationPublicApi organisationPublicApi) {
        val pipelineTasks = new ArrayList<PipelineTask>();

        pipelineTasks.add(new PreCleansingPipelineTask());
        pipelineTasks.add(new PreValidationPipelineTask());

        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi));

        pipelineTasks.add(new PostCleansingPipelineTask());
        pipelineTasks.add(new PostValidationPipelineTask());
        pipelineTasks.add(new LastSanityCheckProcessor(validator));

        return new DefaultBusinessRulesPipelineProcessor(pipelineTasks);
    }

}
