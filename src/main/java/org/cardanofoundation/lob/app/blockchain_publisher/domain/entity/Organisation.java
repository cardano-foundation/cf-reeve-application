package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class Organisation {

    private String id;

    private Currency currency;

}
