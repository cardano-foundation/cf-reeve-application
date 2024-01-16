package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationCurrency;

import java.util.List;
import java.util.Optional;

public interface OrganisationCurrencyRepository {

    List<OrganisationCurrency> listAll();

    Optional<OrganisationCurrency> findByCurrencyId(String currencyId);

    Optional<OrganisationCurrency> findByOrganisationCurrencyInternalId(String currencyId);

}
