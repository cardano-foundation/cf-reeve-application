package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {

    List<Currency> allCurrencies();

    Optional<Currency> findByCurrencyId(String currencyId);

}
