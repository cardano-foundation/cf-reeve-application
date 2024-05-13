package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Optional;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Project {

    private @Size(min = 1, max =  255) String customerCode;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> externalCustomerCode = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> name = Optional.empty();

    public boolean isTheSameBusinessWise() {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.customerCode, this.customerCode);
        equalsBuilder.append(this.externalCustomerCode, this.externalCustomerCode);
        equalsBuilder.append(this.name, this.name);

        return equalsBuilder.isEquals();
    }

}
