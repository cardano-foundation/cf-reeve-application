package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {

    List<Currency> allCurrencies();

    Optional<Currency> findByCurrencyId(String currencyId);

}
