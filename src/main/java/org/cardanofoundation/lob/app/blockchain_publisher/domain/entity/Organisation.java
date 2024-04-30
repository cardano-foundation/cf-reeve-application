package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class Organisation {

    private String id;

    private String name;

    private String taxIdNumber;

    private String countryCode;

    private String currencyId;

}
