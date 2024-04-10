package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
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

    public boolean isTheSameBusinessWise() {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.number, this.number);

        return equalsBuilder.isEquals()
                && this.currency.isTheSameBusinessWise()
                && this.vat.map(Vat::isTheSameBusinessWise).orElse(true)
                && this.counterparty.map(Counterparty::isTheSameBusinessWise).orElse(true);
    }

}
