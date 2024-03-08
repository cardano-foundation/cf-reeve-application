package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Vat {

    @Size(min = 1, max =  255) @NotBlank private String customerCode;

    @Builder.Default
    private Optional<BigDecimal> rate = Optional.empty(); // needed for blockchain data conversion

}
