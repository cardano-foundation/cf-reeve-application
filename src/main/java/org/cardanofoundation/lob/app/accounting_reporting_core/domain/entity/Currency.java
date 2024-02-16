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
@Builder
@Getter
public class Currency {

    @Nullable // sometimes we will resolve this later
    private String id;

    @NotBlank
    private String internalNumber;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

}
