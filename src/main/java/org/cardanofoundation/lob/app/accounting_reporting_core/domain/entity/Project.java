package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class Project {

    @NotBlank
    private String customerCode;

}
