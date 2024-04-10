package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BatchStatistics {

    @Nullable
    private Integer totalTransactionsCount;

    @Nullable
    private Integer processedTransactionsCount;

    @Nullable
    private Integer failedTransactionsCount;

    @Nullable
    private Integer approvedTransactionsCount;

    @Nullable
    private Integer approvedTransactionsDispatchCount;

    @Nullable
    private Integer dispatchedTransactionsCount;

    @Nullable
    private Integer completedTransactionsCount;

    @Nullable
    private Integer finalizedTransactionsCount;

    public Optional<Integer> getTotalTransactionsCount() {
        return Optional.ofNullable(totalTransactionsCount);
    }

    public Optional<Integer> getProcessedTransactionsCount() {
        return Optional.ofNullable(processedTransactionsCount);
    }

    public Optional<Integer> getFailedTransactionsCount() {
        return Optional.ofNullable(failedTransactionsCount);
    }

    public Optional<Integer> getApprovedTransactionsCount() {
        return Optional.ofNullable(approvedTransactionsCount);
    }

    public Optional<Integer> getApprovedTransactionsDispatchCount() {
        return Optional.ofNullable(approvedTransactionsDispatchCount);
    }

    public Optional<Integer> getDispatchedTransactionsCount() {
        return Optional.ofNullable(dispatchedTransactionsCount);
    }

    public Optional<Integer> getCompletedTransactionsCount() {
        return Optional.ofNullable(completedTransactionsCount);
    }

    public Optional<Integer> getFinalizedTransactionsCount() {
        return Optional.ofNullable(finalizedTransactionsCount);
    }

}
