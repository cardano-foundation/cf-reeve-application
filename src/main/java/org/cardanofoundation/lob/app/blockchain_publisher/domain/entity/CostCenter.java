package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class CostCenter {

    private String name;

}
