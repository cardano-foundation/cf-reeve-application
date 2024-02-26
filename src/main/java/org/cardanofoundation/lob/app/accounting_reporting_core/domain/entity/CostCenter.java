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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CostCenter {

    @NotBlank
    private String internalNumber;

    @Nullable
    private String externalNumber;

    @Nullable
    private String name;

    public Optional<String> getExternalNumber() {
        return Optional.ofNullable(externalNumber);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

}
