package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPVersionRelevant;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class Document {

    @LOB_ERPVersionRelevant
    private String num;

    @Embedded
    @NotNull
    @LOB_ERPVersionRelevant
    private Currency currency;

    @Embedded
    @Nullable
    @LOB_ERPVersionRelevant
    private Vat vat;

    @Embedded
    @Nullable
    @LOB_ERPVersionRelevant
    private Counterparty counterparty;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<Counterparty> getCounterparty() {
        return Optional.ofNullable(counterparty);
    }

}
