package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Vat {

    private String internalNumber;

    @Nullable
    private BigDecimal rate;

    public Optional<BigDecimal> getRate() {
        return Optional.ofNullable(rate);
    }

}
