package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.CostCenterMapping;

import java.util.Optional;

public interface CostCenterMappingService {

    Optional<CostCenterMapping> getCostCenter(String organisationId, String internalNumber);

}
