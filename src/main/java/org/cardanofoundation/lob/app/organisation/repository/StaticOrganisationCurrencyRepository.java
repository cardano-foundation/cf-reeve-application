package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationCurrency;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class StaticOrganisationCurrencyRepository implements OrganisationCurrencyRepository {

    private final List<OrganisationCurrency> organisationCurrencies = new ArrayList<>();

    @PostConstruct
    public void init() {
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:CHF",
                "1"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:USD",
                "2"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:CAD",
                "3"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:EUR",
                "4"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:GBP",
                "5"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_24165:ADA:HWGL1C2CK",
                "6"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_24165:BTC:4H95J0R2X",
                "7"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:CRC",
                "8"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_24165:BCH:J9K583ZGG",
                "9"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_24165:BSV:2L8HS2MNP",
                "10"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:LKK",
                "11"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:AED",
                "12"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:GEL",
                "13"
        ));
        organisationCurrencies.add(new OrganisationCurrency(
                "CF",
                "ISO_4217:KRW",
                "14"
        ));
    }

    @Override
    public List<OrganisationCurrency> listAll() {
        return List.copyOf(organisationCurrencies);
    }

    @Override
    public Optional<OrganisationCurrency> findByCurrencyId(String currencyId) {
        return organisationCurrencies.stream()
                .filter(organisationCurrency -> organisationCurrency.currencyId().equals(currencyId))
                .findFirst();
    }

    @Override
    public Optional<OrganisationCurrency> findByOrganisationCurrencyInternalId(String internalId) {
        return organisationCurrencies.stream()
                .filter(organisationCurrency -> organisationCurrency.internalId().equals(internalId))
                .findFirst();
    }

}
