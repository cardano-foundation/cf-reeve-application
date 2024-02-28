package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Counterparty {

    private @Size(min = 1, max =  255) @NotBlank String internalNumber;

    @Builder.Default
    private @Size(min = 1, max =  255) @NotBlank Optional<String> code = Optional.empty(); // this is optional because we do not want to send this to the blockchain

}
