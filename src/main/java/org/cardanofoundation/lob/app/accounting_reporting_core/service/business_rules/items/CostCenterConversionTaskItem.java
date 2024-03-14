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

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.COST_CENTER_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class CostCenterConversionTaskItem implements PipelineTaskItem {

    private final PipelineTask pipelineTask;
    private final OrganisationPublicApi organisationPublicApi;

    @Override
    public TransactionWithViolations run(TransactionWithViolations violationTransaction) {
        val tx = violationTransaction.transaction();
        val organisationId = tx.getOrganisation().getId();

        val violations = new LinkedHashSet<Violation>();

        val txItems = tx.getItems().stream().map(txItem -> {
                    val costCenterM = txItem.getCostCenter();

                    if (costCenterM.isEmpty()) {
                        return txItem;
                    }

                    val costCenter = costCenterM.orElseThrow();
                    val customerCode = costCenter.getCustomerCode();

                    val costCenterMappingM = organisationPublicApi.findCostCenter(organisationId, customerCode);

                    if (costCenterMappingM.isEmpty()) {
                        val v = Violation.create(
                                ERROR,
                                Violation.Source.LOB,
                                organisationId,
                                tx.getId(),
                                txItem.getId(),
                                COST_CENTER_NOT_FOUND,
                                pipelineTask.getClass().getSimpleName(),
                                Map.of(
                                        "customerCode", customerCode,
                                        "transactionNumber", tx.getInternalTransactionNumber()
                                )
                        );

                        violations.add(v);

                        return txItem;
                    }

                    val costCenterMapping = costCenterMappingM.orElseThrow();

                    return txItem.toBuilder()
                            .costCenter(Optional.of(costCenter.toBuilder()
                                    .customerCode(customerCode)
                                    .externalCustomerCode(Optional.of(costCenterMapping.getExternalCustomerCode()))
                                    .name(Optional.of(costCenterMapping.getName()))
                                    .build())
                            )
                            .build();
                })
                .collect(Collectors.toSet());

        if (!violations.isEmpty()) {
            return violationTransaction;
        }

        return TransactionWithViolations.create(tx.toBuilder()
                .items(txItems)
                .build());
    }

}
