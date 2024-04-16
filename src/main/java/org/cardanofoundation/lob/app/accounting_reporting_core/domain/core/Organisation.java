package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Organisation {

    @Size(min = 1, max =  255) @NotBlank  private String id;

    @Builder.Default
    private Optional<@Size(min = 1, max =  50) String> shortName = Optional.empty();

    @Size(min = 1, max =  255) @NotBlank
    private String currencyId;

}
