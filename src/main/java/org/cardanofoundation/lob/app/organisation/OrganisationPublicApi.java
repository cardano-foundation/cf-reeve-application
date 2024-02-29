package org.cardanofoundation.lob.app.organisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.*;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationVatRepository;
import org.cardanofoundation.lob.app.organisation.service.*;
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
    private final ProjectCodeMappingService projectCodeMappingService;
    private final ChartOfAccountsService chartOfAccountsService;

    public List<Organisation> listAll() {
        return organisationService.listAll();
    }

    public Optional<Organisation> findByOrganisationId(String id) {
        return organisationService.findById(id);
    }

    public Optional<Organisation> findByErpInternalNumber(ERPDataSource erpDataSource, String erpInternalNumber) {
        return organisationService.findByERPSystemId(erpDataSource, erpInternalNumber);
    }

    public Optional<OrganisationCurrency> findOrganisationCurrencyByInternalId(String internalCurrencyId) {
        return currencyService.findByOrganisationCurrencyInternalId(internalCurrencyId);
    }

    public Optional<OrganisationVat> findOrganisationVatByInternalId(String organisationId, String internalNumber) {
        return organisationVatRepository.findByOrganisationAndInternalNumber(organisationId, internalNumber);
    }

    public Optional<CostCenterMapping> findCostCenter(String organisationId, String internalNumber) {
        return costCenterMappingService.getCostCenter(organisationId, internalNumber);
    }

    public Optional<ProjectMapping> findProject(String organisationId, String internalNumber) {
        return projectCodeMappingService.getProject(organisationId, internalNumber);
    }

    public Optional<CharterOfAccounts> getChartOfAccounts(String accountCode) {
        return chartOfAccountsService.getChartAccount(accountCode);
    }

    public Optional<CharterOfAccounts> getChartOfAccounts(ERPDataSource erpDataSource, String internalNumber) {
        return chartOfAccountsService.getChartAccount(erpDataSource, internalNumber);
    }

}
