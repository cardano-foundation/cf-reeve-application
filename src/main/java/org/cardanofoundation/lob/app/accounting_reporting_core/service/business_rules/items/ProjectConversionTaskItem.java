package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.PROJECT_CODE_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class ProjectConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApi organisationPublicApi;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();

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
                                tx.getOrganisation().getId(),
                                tx.getId(),
                                PROJECT_CODE_NOT_FOUND,
                                pipelineTask.getClass().getSimpleName(),
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
            return TransactionWithViolations.create(tx.toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    violations);
        }

        return TransactionWithViolations.create(tx.toBuilder()
                .items(txItems)
                .build());
    }

}
