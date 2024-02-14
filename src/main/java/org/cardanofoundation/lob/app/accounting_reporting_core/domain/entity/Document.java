package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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
public class Document {

    private String internalNumber;

    @Embedded
    private Currency currency;

    @Embedded
    @Nullable
    private Vat vat;

    @Embedded
    @Nullable
    private Counterparty counterparty;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<Counterparty> getCounterparty() {
        return Optional.ofNullable(counterparty);
    }

}
