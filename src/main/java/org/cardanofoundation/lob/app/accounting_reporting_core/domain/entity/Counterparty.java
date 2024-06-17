package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPVersionRelevant;

import javax.annotation.Nullable;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class Counterparty {

    @LOB_ERPVersionRelevant
    private String customerCode;

    @Enumerated(STRING)
    @LOB_ERPVersionRelevant
    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.Type type;

    @Nullable
    @LOB_ERPVersionRelevant
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

}
