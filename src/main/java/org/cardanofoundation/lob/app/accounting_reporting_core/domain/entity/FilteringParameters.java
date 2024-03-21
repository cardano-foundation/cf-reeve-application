package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import java.time.LocalDate;
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

    @Builder.Default
    @NotNull
    @ElementCollection
    @CollectionTable(
            name = "accounting_core_transaction_filtering_params_transaction_number",
            joinColumns = @JoinColumn(name = "owner_id")
    )
    @Column(name = "transaction_number")
    private List<String> transactionNumbers = List.of();

}
