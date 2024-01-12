package org.cardanofoundation.lob.app.adapter.netsuite.service;

import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter {

    // TODO this is complete bollocks, fill it out with proper currency code mappings
    public String convert(int currency) {
        return switch (currency) {
            case 1 -> "CHF";
            case 2 -> "EUR";
            case 3 -> "GBP";
            case 4 -> "ADA";

            default -> "?";
            //default -> throw new IllegalStateException("Unexpected netsuite currency value: " + currency);
        };
    }

}

