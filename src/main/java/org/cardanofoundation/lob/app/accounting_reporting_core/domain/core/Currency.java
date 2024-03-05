package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.IsoStandard.ISO_4217;

@AllArgsConstructor
@Getter
@Builder
public class Currency {

    @Builder.Default
    private IsoStandard currencyISOStandard = ISO_4217;

    @Pattern(regexp = "^[A-Z]{3,6}$")
    private String currencyISOCode;

    @Builder.Default
    private Optional<String> isoUniqueId = Optional.empty();

    public static Currency fromId(String id) {
        if (id.split(":").length == 3) {
            return Currency.builder()
                    .currencyISOStandard(IsoStandard.valueOf(id.split(":")[0]))
                    .currencyISOCode(id.split(":")[1])
                    .isoUniqueId(Optional.of(id.split(":")[2]))
                    .build();
        }

        return Currency.builder()
                .currencyISOStandard(IsoStandard.valueOf(id.split(":")[0]))
                .currencyISOCode(id.split(":")[1])
                .build();
    }

    public String toId() {
        if (isoUniqueId.isEmpty()) {
            return STR."\{currencyISOStandard}:\{currencyISOCode}";
        }

        return STR."\{currencyISOStandard}:\{currencyISOCode}:\{isoUniqueId.orElseThrow()}";
    }

    public enum IsoStandard {
        ISO_4217,
        ISO_24165
    }

}
