package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Project {

    private @Size(min = 1, max =  255) String customerCode;

    public boolean isTheSameBusinessWise() {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.customerCode, this.customerCode);

        return equalsBuilder.isEquals();
    }

}
