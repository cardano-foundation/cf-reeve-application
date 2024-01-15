package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import jakarta.annotation.PostConstruct;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;

import java.util.ArrayList;
import java.util.List;

public class CurrencyRepository {

    private List<Currency> currencies = new ArrayList<>();

    @PostConstruct
    public void init() {
        java.util.Currency.getAvailableCurrencies().forEach(c -> {
            currencies.add(new Currency(c.getCurrencyCode(), c.getDisplayName()));
        });

        currencies.add(new Currency("ADA", "Cardano"));
        currencies.add(new Currency("ETH", "Ethereum"));
        currencies.add(new Currency("BTC", "Bitcoin"));
    }

    public List<Currency> listCurrencies() {
        return List.copyOf(currencies);
    }

}
