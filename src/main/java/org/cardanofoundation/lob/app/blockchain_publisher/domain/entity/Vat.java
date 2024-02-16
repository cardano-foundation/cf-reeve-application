package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import javax.annotation.Nullable;
import java.math.BigDecimal;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class Vat {

    @Nullable
    private String internalNumber;

    @Nullable
    private BigDecimal rate;

}
