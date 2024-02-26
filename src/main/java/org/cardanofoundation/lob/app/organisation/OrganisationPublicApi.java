package org.cardanofoundation.lob.app.organisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.*;
import org.cardanofoundation.lob.app.organisation.repository.CostCenterMappingService;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationVatRepository;
import org.cardanofoundation.lob.app.organisation.repository.ProjectMappingService;
import org.cardanofoundation.lob.app.organisation.service.CurrencyService;
import org.cardanofoundation.lob.app.organisation.service.OrganisationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganisationPublicApi {

    private final OrganisationService organisationService;
    private final CurrencyService currencyService;
    private final OrganisationVatRepository organisationVatRepository;
    private final CostCenterMappingService costCenterMappingService;
    private final ProjectMappingService projectMappingService;

    public List<Organisation> listAll() {
        return organisationService.listAll();
    }

    public Optional<Organisation> findBy(String connectorId,
                                         String foreignSystemId) {
        return organisationService.findBy(connectorId, foreignSystemId);
    }

    public Optional<Organisation> findByOrganisationId(String id) {
        return organisationService.findById(id);
    }

    public Optional<OrganisationCurrency> findOrganisationCurrencyByInternalId(String internalCurrencyId) {
        return currencyService.findByOrganisationCurrencyInternalId(internalCurrencyId);
    }

    public Optional<OrganisationVat> findOrganisationVatByInternalId(String organisationId, String internalNumber) {
        return organisationVatRepository.findByOrganisationAndInternalNumber(organisationId, internalNumber);
    }

    public Optional<Currency> findByCurrencyId(String currencyId) {
        return currencyService.findByCurrencyId(currencyId);
    }

    public Optional<CostCenterMapping> findCostCenterMapping(String organisationId, String costCenterInternalNumber) {
        return costCenterMappingService.getCostCenter(organisationId, costCenterInternalNumber);
    }

    public Optional<ProjectMapping> findProject(String organisationId, String costCenterInternalNumber) {
        return projectMappingService.getProject(organisationId, costCenterInternalNumber);
    }

}
