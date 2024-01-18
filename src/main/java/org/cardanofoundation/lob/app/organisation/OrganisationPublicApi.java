package org.cardanofoundation.lob.app.organisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.*;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationVatRepository;
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

    public List<Organisation> listAll() {
        return organisationService.listAll();
    }

    public Optional<Organisation> findByForeignProvider(String foreignId,
                                                        AccountSystemProvider accountSystemProvider) {
        return organisationService.findByForeignProvider(foreignId, accountSystemProvider);
    }

    public Optional<OrganisationCurrency> findOrganisationCurrencyByInternalId(String internalCurrencyId) {
        return currencyService.findByOrganisationCurrencyInternalId(internalCurrencyId);
    }

    public Optional<OrganisationVat> findOrganisationVatByInternalId(String internalVatId) {
        return organisationVatRepository.findByInternalId(internalVatId);
    }

    public Optional<Currency> findByCurrencyId(String currencyId) {
        return currencyService.findByCurrencyId(currencyId);
    }

}
