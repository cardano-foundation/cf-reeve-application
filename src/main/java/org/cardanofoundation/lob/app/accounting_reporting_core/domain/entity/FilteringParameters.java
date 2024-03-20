package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class FilteringParameters {

    @Column(name = "organisation_id", nullable = false)
    @NotNull
    private String organisationId;

    @Builder.Default
    @Enumerated(STRING)
    @Column(name = "filtering_parameters_transaction_types")
    @Convert(converter = CSVTransactionTypeConverter.class)
    private List<TransactionType> transactionTypes = List.of();

    @Column(name = "from_date", nullable = false)
    private LocalDate from;

    @Column(name = "to_date", nullable = false)
    private LocalDate to;

    @Column(name = "transaction_number")
    @Nullable
    private String transactionNumber;

    public Optional<String> getTransactionNumber() {
        return Optional.ofNullable(transactionNumber);
    }

}
