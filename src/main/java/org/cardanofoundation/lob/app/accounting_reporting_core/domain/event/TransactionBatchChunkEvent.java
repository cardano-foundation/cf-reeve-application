package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Builder
@Getter
@DomainEvent
public class TransactionBatchChunkEvent {

    private String chunkId;

    private String batchId;

    private String organisationId;

    @Builder.Default
    private int chunkNo = -1;

    @Builder.Default
    private int totalChunksCount = -1;

    private int totalTransactionsCount;

    @Builder.Default
    private Set<Transaction> transactions = Set.of();

    private LocalDateTime startTime;

    @Builder.Default
    private Optional<LocalDateTime> finishTime = Optional.empty();

    @Builder.Default
    private Status status = Status.STARTED;

    public enum Status {
        STARTED, PROCESSING, FINISHED
    }

}
