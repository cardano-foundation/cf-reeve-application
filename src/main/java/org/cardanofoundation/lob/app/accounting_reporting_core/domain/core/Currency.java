package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Pattern;

public record Currency(
                       String id,
                       CurrencyIsoStandard currencyISOStandard,
                       @Pattern(regexp = "[A-Z]{3,6}$")
                       String currencyISOCode,
                       String currencyName) {

    public static String id(CurrencyIsoStandard currencyISOStandard, String currencyISOCode) {
        return currencyISOStandard + "::" + currencyISOCode;
    }

    public enum CurrencyIsoStandard {
        ISO_4217,
        ISO_24165
    }

}
