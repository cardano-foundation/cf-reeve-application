package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Organisation {

    private String id;

    @Embedded
    private Currency currency;

}
