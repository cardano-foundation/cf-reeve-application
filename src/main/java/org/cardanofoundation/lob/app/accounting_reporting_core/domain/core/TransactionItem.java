package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsHex;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionItem {

    @NotBlank String id;

    @NotNull BigDecimal amountFcy;

    @NotNull BigDecimal amountLcy;

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountCodeDebit = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountNameDebit = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountCodeCredit = Optional.empty();

    public static String id(String organisationId,
                            String internalTransactionNumber,
                            String lineNo) {
        return digestAsHex(STR."\{organisationId}::\{internalTransactionNumber}::\{lineNo}");
    }

}
