package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder
public class Currency {

    private Optional<@Size(min = 1, max =  255) String> id;

    @Size(min = 1, max =  255) @NotBlank private String internalNumber;

    public static Currency from(@Size(min = 1, max =  255) @NotBlank String internalNumber) {
        return new Currency(Optional.empty(), internalNumber);
    }

}

