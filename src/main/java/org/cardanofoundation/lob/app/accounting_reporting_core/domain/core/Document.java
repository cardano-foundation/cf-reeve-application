package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public class Document {

    @Size(min = 1, max =  255) @NotBlank private String number;

    @NotNull
    private Currency currency;

    @Builder.Default
    @NotNull
    private Optional<Vat> vat = Optional.empty();

    @Builder.Default
    @NotNull
    private Optional<Counterparty> counterparty = Optional.empty();

}
