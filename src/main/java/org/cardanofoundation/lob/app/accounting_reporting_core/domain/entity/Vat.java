package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.math.BigDecimal;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Vat {

    @Column(name = "vat_internal_code")
    private String internalCode;

    @Column(name = "vat_rate")
    @Nullable
    private BigDecimal rate;

}
