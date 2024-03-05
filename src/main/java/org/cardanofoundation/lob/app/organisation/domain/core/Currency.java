package org.cardanofoundation.lob.app.organisation.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder
public class Currency {

    @Builder.Default
    @NotNull
    private IsoStandard currencyISOStandard = IsoStandard.ISO_4217;

    @NotNull
    @Pattern(regexp = "^[A-Z]{3,6}$")
    private String currencyISOCode;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> isoUniqueId = Optional.empty();

    @Size(min = 1, max =  255)
    @NotBlank
    private String name;

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
