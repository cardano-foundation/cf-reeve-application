package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.PROJECT_CODE_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class ProjectConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public Transaction run(Transaction tx) {
        val violations = new LinkedHashSet<Violation>();

        val txItems = tx.getItems().stream().map(txItem -> {
                    val projectM = txItem.getProject();

                    if (projectM.isEmpty()) {
                        return txItem;
                    }

                    val project = projectM.orElseThrow();

                    val organisationId = tx.getOrganisation().getId();
                    val customerCode = projectM.orElseThrow().getCustomerCode();

                    val projectMappingM = organisationPublicApi.findProject(organisationId, customerCode);

                    if (projectMappingM.isEmpty()) {
                        val v = Violation.create(
                                ERROR,
                                Violation.Source.LOB,
                                txItem.getId(),
                                PROJECT_CODE_NOT_FOUND,
                                this.getClass().getSimpleName(),
                                Map.of(
                                        "transactionNumber", tx.getInternalTransactionNumber()
                                )
                        );

                        violations.add(v);

                        return txItem;
                    }

                    val projectMapping = projectMappingM.orElseThrow();

                    return txItem.toBuilder()
                            .project(Optional.of(project.toBuilder()
                                    .customerCode(projectMapping.getId().getCustomerCode())
                                    .build()))
                            .build();
                })
                .collect(Collectors.toSet());

        if (!violations.isEmpty()) {
            return tx.toBuilder()
                    .validationStatus(FAILED)
                    .violations(Stream.concat(tx.getViolations().stream(), violations.stream()).collect(Collectors.toSet()))
                    .build();
        }

        return tx.toBuilder()
                .items(txItems)
                .build();
    }

}
