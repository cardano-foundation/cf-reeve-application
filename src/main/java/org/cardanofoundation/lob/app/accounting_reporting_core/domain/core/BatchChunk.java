package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Builder
@Getter
public class BatchChunk {

    private String batchId;

    private String organisationId;

    @Builder.Default
    private Set<Transaction> transactions = Set.of();

    @Builder.Default
    private FilteringParameters filteringParameters = FilteringParameters.EMPTY;

    private String issuedBy;
    private LocalDateTime startTime;

    @Builder.Default
    private Optional<LocalDateTime> finishTime = Optional.empty();

    @Builder.Default
    private Status status = Status.STARTED;

    public enum Status {
        STARTED, FINISHED
    }

}
