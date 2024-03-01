package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.Set;

public interface PipelineTask {

    TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                             OrganisationTransactions ignoredOrganisationTransactions,
                             Set<Violation> allViolationUntilNow);

}
