package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Organisation {

    @NotBlank
    private String id;

    @Nullable
    private String shortName;

    @Nullable
    private Currency currency;

    public Optional<String> getShortName() {
        return Optional.ofNullable(shortName);
    }

    public Optional<Currency> getCurrency() {
        return Optional.ofNullable(currency);
    }

}
