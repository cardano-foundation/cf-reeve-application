package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Currency {

    @NotBlank
    private String customerCode;

    @Builder.Default
    private Optional<CoreCurrency> coreCurrency = Optional.empty();

    public boolean isTheSameBusinessWise() {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.customerCode, this.customerCode);

        return equalsBuilder.isEquals()
                && this.coreCurrency.map(CoreCurrency::isTheSameBusinessWise).orElse(true);
    }

}
