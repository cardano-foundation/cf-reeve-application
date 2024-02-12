package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
@Embeddable
public class Document {

    private String internalDocumentNumber;

    @Embedded
    @Nullable
    private Vat vat;

    @Nullable
    @Embedded
    private Vendor vendor;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<Vendor> getVendor() {
        return Optional.ofNullable(vendor);
    }

}
