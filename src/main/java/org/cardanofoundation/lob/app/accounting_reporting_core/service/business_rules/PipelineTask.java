package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;

public interface PipelineTask {

    TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                             OrganisationTransactions ignoredOrganisationTransactions);

}
