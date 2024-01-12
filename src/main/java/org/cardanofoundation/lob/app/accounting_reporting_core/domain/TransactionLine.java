package org.cardanofoundation.lob.app.accounting_reporting_core.domain;

import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public record TransactionLine(

        // mandatory values
        String organisationId,

        TransactionType transactionType,

        LocalDateTime entryDate,

        String transactionNumber,

        String accountCodeDebit,

        @Pattern(regexp = "^[A-Z]{3}$")
        String baseCurrency,

        // target currency
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency,

        BigDecimal fxRate,

        /// optionals below

        Optional<String> documentNumber,

        Optional<String> vendorCode,

        Optional<String> vendorName,

        Optional<String> costCenter,

        Optional<String> projectCode,

        Optional<String> vatCode,

        Optional<String> accountNameDebit,

        Optional<String> accountCredit,

        Optional<String> memo,

        Optional<BigDecimal> amountFcy,

        Optional<BigDecimal> amountLcy) {

}
