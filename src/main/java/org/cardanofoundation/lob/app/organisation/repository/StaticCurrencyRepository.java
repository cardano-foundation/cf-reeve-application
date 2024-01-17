package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.organisation.domain.core.Currency;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Locale.ENGLISH;
import static org.cardanofoundation.lob.app.organisation.domain.core.Currency.CurrencyIsoStandard.ISO_24165;
import static org.cardanofoundation.lob.app.organisation.domain.core.Currency.CurrencyIsoStandard.ISO_4217;

@Service
@Slf4j
public class StaticCurrencyRepository implements CurrencyRepository {

    private List<Currency> currencies = new ArrayList<>();

    @PostConstruct
    public void init() {
        java.util.Currency.getAvailableCurrencies().forEach(currency -> {
            val currencyId = Currency.id(ISO_4217, currency.getCurrencyCode());

            val c = new Currency(
                    currencyId,
                    ISO_4217,
                    currency.getCurrencyCode(),
                    currency.getDisplayName(ENGLISH)
            );

            currencies.add(c);
        });

        currencies.add(new Currency(
                Currency.id(ISO_24165, "ADA"),
                ISO_24165,
                "ADA",
                "Cardano"
        ));

        currencies.add(new Currency(
                Currency.id(ISO_24165, "BTC"),
                ISO_24165,
                "BTC",
                "Bitcoin"
        ));

        currencies.add(new Currency(
                Currency.id(ISO_24165, "BSV"),
                ISO_24165,
                "BSV",
                "Bitcoin Satoshi Vision"
        ));

        currencies.add(new Currency(
                Currency.id(ISO_24165, "BCH"),
                ISO_24165,
                "BCH",
                "Bitcoin Cash"
        ));

        log.info("StaticCurrencyRepository init completed.");
    }

    @Override
    public List<Currency> allCurrencies() {
        return List.copyOf(currencies);
    }

    @Override
    public Optional<Currency> findByCurrencyId(String currencyId) {
        return currencies.stream()
                .filter(currency -> currency.id().equals(currencyId))
                .findFirst();
    }

}
