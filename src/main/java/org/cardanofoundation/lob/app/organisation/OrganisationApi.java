package org.cardanofoundation.lob.app.organisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationCurrency;
import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationVat;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationCurrencyRepository;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationVatRepository;
import org.cardanofoundation.lob.app.organisation.service.OrganisationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganisationApi {

    private final OrganisationService organisationService;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final OrganisationVatRepository organisationVatRepository;

    public Optional<Organisation> findByForeignProvider(String foreignId,
                                                        AccountSystemProvider accountSystemProvider) {
        return organisationService.findByForeignProvider(foreignId, accountSystemProvider);
    }

    public Optional<OrganisationCurrency> findOrganisationCurrencyByInternalId(String internalCurrencyId) {
        return organisationCurrencyRepository.findByOrganisationCurrencyInternalId(internalCurrencyId);
    }

    public Optional<OrganisationVat> findOrganisationVatByInternalId(String internalVatId) {
        return organisationVatRepository.findByInternalId(internalVatId);
    }

}
