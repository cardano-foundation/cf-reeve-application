package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class Organisation {

    @NotBlank
    private String id;

    @Nullable
    private String shortName;

    private String currencyId;

    public Optional<String> getShortName() {
        return Optional.ofNullable(shortName);
    }

}
