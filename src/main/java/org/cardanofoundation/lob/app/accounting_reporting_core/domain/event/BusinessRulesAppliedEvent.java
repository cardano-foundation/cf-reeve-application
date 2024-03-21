package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jmolecules.event.annotation.DomainEvent;

import java.util.Optional;


@AllArgsConstructor
@Builder
@DomainEvent
@Getter
public class BusinessRulesAppliedEvent {

    @NotBlank
    private String organisationId;

    @NotBlank
    private String batchId;

    @Builder.Default
    private Optional<Integer> totalTransactionsCount = Optional.empty();

}
