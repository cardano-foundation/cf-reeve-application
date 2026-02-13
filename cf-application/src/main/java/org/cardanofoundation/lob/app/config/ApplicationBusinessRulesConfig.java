package org.cardanofoundation.lob.app.config;

import jakarta.validation.Validator;
import org.cardanofoundation.lob.app.accounting_reporting_core.config.BusinessRulesConfig;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DefaultPipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationBusinessRulesConfig extends BusinessRulesConfig {

    public ApplicationBusinessRulesConfig(Validator validator, OrganisationPublicApi organisationPublicApi, CoreCurrencyRepository currencyRepository) {
        super(validator, organisationPublicApi, currencyRepository);
    }

    protected PipelineTask preValidationPipelineTask() {
        return new DefaultPipelineTask(List.of(
                new AmountsFcyCheckTaskItem(),
                new AmountsLcyCheckTaskItem(),
                new JournalAccountCreditEnrichmentTaskItem(organisationPublicApi)
        ));
    }

}
