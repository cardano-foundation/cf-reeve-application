package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.Map;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class BatchDetails {

    private String code;
    private String subCode;

    @Builder.Default
    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private Map<String, Object> bag = new HashMap<>();

}
