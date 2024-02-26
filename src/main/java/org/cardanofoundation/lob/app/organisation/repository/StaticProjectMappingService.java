package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.ProjectMapping;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class StaticProjectMappingService implements ProjectMappingService {

    private final Map<MappingKey, ProjectMapping> mappings = new HashMap<>();

    @PostConstruct
    public void init() {
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "7"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "8"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "9"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "10"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "11"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "12"), new CostCenterMapping("6", "OPERATIONS"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "10"), new CostCenterMapping("6", "OPERATIONS"));
//
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "14"), new CostCenterMapping("13", "ENTERPRISE_TECHNOLOGY"));
//        mappings.put(new StaticCostCenterMappingService.MappingKey(Organisation.testOrgId(), "15"), new CostCenterMapping("13", "ENTERPRISE_TECHNOLOGY"));
    }
    @Override
    public Optional<ProjectMapping> getProject(String organisationId,
                                               String internalNumber) {
        return Optional.of(new ProjectMapping(STR."C:\{internalNumber}"));
    }

    record MappingKey(String organisationId, String internalNumber) { }

}
