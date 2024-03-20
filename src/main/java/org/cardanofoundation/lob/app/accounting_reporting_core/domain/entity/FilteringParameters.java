package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class FilteringParameters {

    @Column(name = "organisation_id")
    @Nullable
    private String organisationId;

    @Builder.Default
//    @ElementCollection(targetClass = TransactionType.class)
//    @Enumerated(EnumType.STRING)
//    @CollectionTable(name = "filtering_parameters_transaction_type")
//    @Column(name = "transaction_type")
//    @Getter
    private List<String> transactionTypes = List.of();

    @Nullable
    @Column(name = "from_date")
    private LocalDate from;

    @Nullable
    @Column(name = "to_date")
    private LocalDate to;

    @Nullable
    @Column(name = "transaction_number")
    private String transactionNumber;

    public Optional<String> getOrganisationId() {
        return Optional.ofNullable(organisationId);
    }

    public Optional<LocalDate> getFrom() {
        return Optional.ofNullable(from);
    }

    public Optional<LocalDate> getTo() {
        return Optional.ofNullable(to);
    }

    public Optional<String> getTransactionNumber() {
        return Optional.ofNullable(transactionNumber);
    }

}
