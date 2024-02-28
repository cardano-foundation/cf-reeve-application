package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.CostCenterMapping;
import org.cardanofoundation.lob.app.organisation.repository.CostCenterMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CostCenterMappingService {

    private final CostCenterMappingRepository costCenterMappingRepository;

    public Optional<CostCenterMapping> getCostCenter(String organisationId, String internalNumber){
        return costCenterMappingRepository.getCostCenter(organisationId, internalNumber);
    }

}
