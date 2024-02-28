package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.CostCenterMapping;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class StaticCostCenterMappingRepository implements CostCenterMappingRepository {

    private final Map<MappingKey, CostCenterMapping> mappings = new HashMap<>();

    @PostConstruct
    public void init() {
        mappings.put(new MappingKey(Organisation.testOrgId(), "7"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "8"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "9"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "10"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "11"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "12"), new CostCenterMapping("6", "OPERATIONS"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "10"), new CostCenterMapping("6", "OPERATIONS"));

        mappings.put(new MappingKey(Organisation.testOrgId(), "14"), new CostCenterMapping("13", "ENTERPRISE_TECHNOLOGY"));
        mappings.put(new MappingKey(Organisation.testOrgId(), "15"), new CostCenterMapping("13", "ENTERPRISE_TECHNOLOGY"));
    }

    @Override
    public Optional<CostCenterMapping> getCostCenter(String organisationId, String internalNumber) {
        return Optional.ofNullable(mappings.get(new MappingKey(organisationId, internalNumber))).or(() -> {
            log.warn("Cost center mapping not found for organisationId: {} and externalNumber: {}", organisationId, internalNumber);

            return Optional.of(new CostCenterMapping(internalNumber, "UNKNOWN"));
        });
    }

    record MappingKey(String organisationId, String internalNumber) { }

}
