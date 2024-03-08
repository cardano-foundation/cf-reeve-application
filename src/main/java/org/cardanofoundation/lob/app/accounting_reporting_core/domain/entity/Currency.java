package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Currency {

    @Nullable
    private String id;

    private String customerCode;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

}
