package org.cardanofoundation.lob.app.organisation.domain.core;

import jakarta.validation.constraints.Pattern;

import java.util.Optional;

public record Currency(
                       String id,
                       CurrencyIsoStandard currencyISOStandard,
                       @Pattern(regexp = "^[A-Z]{3,6}$")
                       String currencyISOCode,
                       Optional<String> isoUniqueId,
                       String currencyName) {

    public static String id(CurrencyIsoStandard currencyISOStandard, String currencyISOCode, Optional<String> isoUniqueId) {
        if (isoUniqueId.isEmpty()) {
            return STR . "\{currencyISOStandard}:\{currencyISOCode}";
        }

        return STR . "\{currencyISOStandard}:\{currencyISOCode}:\{isoUniqueId.orElseThrow()}";
    }

    public enum CurrencyIsoStandard {
        ISO_4217,
        ISO_24165
    }

}
