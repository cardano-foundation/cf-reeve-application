package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.CoreCurrencyRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class ConversionsPipelineTask implements PipelineTask {

    private final OrganisationPublicApi organisationPublicApi;
    private final CoreCurrencyRepository coreCurrencyRepository;

    public TransformationResult run(OrganisationTransactions passedOrganisationTransactions,
                                    OrganisationTransactions ignoredOrganisationTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val organisationId = passedOrganisationTransactions.organisationId();

        val passedTransactions = passedOrganisationTransactions
                .transactions().stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(tx -> new OrganisationConversionTaskItem(this, organisationPublicApi, coreCurrencyRepository).run(tx))
                .map(tx -> new DocumentConversionTaskItem(this, organisationPublicApi, coreCurrencyRepository).run(tx))
                .map(tx -> new CostCenterConversionTaskItem(this, organisationPublicApi).run(tx))
                .map(tx -> new ProjectConversionTaskItem(this, organisationPublicApi).run(tx))
                .map(tx -> new AccountEventCodesConversionTaskItem(this, organisationPublicApi).run(tx))
                .toList();

        val newViolations = new HashSet<Violation>();
        val finalTransactions = new LinkedHashSet<Transaction>();

        for (val transactions : passedTransactions) {
            finalTransactions.add(transactions.transaction());
            newViolations.addAll(transactions.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(organisationId, finalTransactions),
                ignoredOrganisationTransactions,
                newViolations
        );
    }

}
