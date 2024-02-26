package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class CostCenter {

    private @Size(min = 1, max =  255) String internalNumber;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> externalNumber = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  25) String> code = Optional.empty();

}
