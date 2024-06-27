package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class FilteringParameters {

    @NotNull
    private String organisationId;

    @Builder.Default
    @NotNull
    private List<TransactionType> transactionTypes = List.of();

    @NotNull
    private LocalDate from;

    @NotNull
    private LocalDate to;

    @Nullable // nullable since when batch fails we don't always have this info (it is resolved from the org)
    private YearMonth accountingPeriodFrom;

    @Nullable // nullable since when batch fails we don't always have this info (it is resolved from the org)
    private YearMonth accountingPeriodTo;

    @Builder.Default
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "accounting_core_transaction_filtering_params_transaction_number",
            joinColumns = @JoinColumn(name = "owner_id")
    )
    @Column(name = "transaction_number")
    private List<String> transactionNumbers = List.of();

}
