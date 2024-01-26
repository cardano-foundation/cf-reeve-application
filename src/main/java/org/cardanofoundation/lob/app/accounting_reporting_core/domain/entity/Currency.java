package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Currency {

    @Column(name = "currency_id")
    @Nullable
    private String id;

    @Column(name = "currency_internal_code", nullable = false)
    private String internalCode;

}
