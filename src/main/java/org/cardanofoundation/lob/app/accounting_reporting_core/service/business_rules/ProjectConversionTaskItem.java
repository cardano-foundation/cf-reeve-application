package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.PipelineTaskItem;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;

import java.util.Map;
import java.util.Optional;

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

        val projectM = tx.getProject();

        if (projectM.isEmpty()) {
            return violationTransaction;
        }

        val project = projectM.orElseThrow();

        val organisationId = tx.getOrganisation().getId();
        val customerCode = project.getCustomerCode();

        val projectMappingM = organisationPublicApi.findProject(organisationId, customerCode);

        if (projectMappingM.isEmpty()) {
            val v = Violation.create(
                    ERROR,
                    organisationId,
                    tx.getId(),
                    PROJECT_CODE_NOT_FOUND,
                    pipelineTask.getClass().getSimpleName(),
                    Map.of(
                            "customerCode", customerCode,
                            "transactionNumber", tx.getInternalTransactionNumber()
                    )
            );

            return TransactionWithViolations.create(tx
                            .toBuilder()
                            .validationStatus(FAILED)
                            .build(),
                    v);
        }

        val projectMapping = projectMappingM.orElseThrow();

        return TransactionWithViolations.create(tx.toBuilder()
                .project(Optional.of(project.toBuilder()
                        .customerCode(projectMapping.getId().getCustomerCode())
                        .build()))
                .build());
    }

}
