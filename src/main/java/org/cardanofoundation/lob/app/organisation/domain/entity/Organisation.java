package org.cardanofoundation.lob.app.organisation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity(name = "organisation")
public class Organisation {

    @Id
    @Column(name = "organisation_id", nullable = false)
    private String id;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "long_name", nullable = false)
    private String longName;

    @Column(name = "vat_number", nullable = false)
    private String vatNumber;

    @Column(name = "currency_id", nullable = false)
    private String currencyId;

    public static String id(String vatId) {
        return digestAsHex(vatId);
    }

}
