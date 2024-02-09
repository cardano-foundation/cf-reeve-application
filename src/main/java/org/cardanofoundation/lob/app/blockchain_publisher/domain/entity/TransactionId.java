package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransactionId implements Serializable {

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

}
