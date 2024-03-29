package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type.VENDOR;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Counterparty {

    private @Size(min = 1, max =  255) @NotBlank String customerCode;

    @Builder.Default
    private Type type = VENDOR;

    @Builder.Default
    private @Size(min = 1, max =  255) @NotBlank Optional<String> name = Optional.empty(); // this is optional because we do not want to send this to the blockchain, so PII is not exposed

    public enum Type {
        EMPLOYEE,
        VENDOR,
        DONOR,
        CLIENT
    }

    public boolean isTheSameBusinessWise() {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.customerCode, this.customerCode);
        equalsBuilder.append(this.name, this.name);
        equalsBuilder.append(this.type, this.type);

        return equalsBuilder.isEquals();
    }

}
