package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FilteringParameters {

    @NotBlank
    private String organisationId;

    @Builder.Default
    private List<TransactionType> transactionTypes = List.of();

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    @Builder.Default
    private Optional<String> transactionNumber = Optional.empty();

    public static FilteringParameters acceptAll(String organisationId,
                                                LocalDate from,
                                                LocalDate to) {
        return FilteringParameters.builder()
                .organisationId(organisationId)
                .from(from)
                .to(to)
                .build();
    }

}
