package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.COST_CENTER_NOT_FOUND;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
public class CostCenterConversionTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApi;

    @Override
    public Transaction run(Transaction tx) {
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
                                COST_CENTER_NOT_FOUND,
                                this.getClass().getSimpleName(),
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
