package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.Currency;
import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationCurrency;
import org.cardanofoundation.lob.app.organisation.repository.CurrencyRepository;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationCurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;

    //@Transactional(readOnly = true)
    public Optional<Currency> findByCurrencyId(String currencyId) {
        return currencyRepository.findByCurrencyId(currencyId);
    }

    //@Transactional(readOnly = true)
    public Optional<OrganisationCurrency> findByOrganisationCurrencyInternalId(String internalCurrencyId) {
        return organisationCurrencyRepository.findByOrganisationCurrencyInternalId(internalCurrencyId);
    }

}
