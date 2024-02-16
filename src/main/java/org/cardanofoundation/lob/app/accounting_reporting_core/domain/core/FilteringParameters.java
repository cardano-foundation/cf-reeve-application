package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

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

    public static final FilteringParameters EMPTY = FilteringParameters.builder()
            .build();

    @Builder.Default
    private List<TransactionType> transactionTypes = List.of();

    @Builder.Default
    private Optional<LocalDate> from = Optional.empty();

    @Builder.Default
    private Optional<LocalDate> to = Optional.empty();

    @Builder.Default
    private List<String> organisationIds = List.of();

    @Builder.Default
    private List<String> projectInternalNumbers = List.of();

}
