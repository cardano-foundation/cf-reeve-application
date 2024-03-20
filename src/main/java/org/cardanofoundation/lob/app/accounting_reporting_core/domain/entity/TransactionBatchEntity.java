package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BatchStatus;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Accessors(fluent = true)
@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionBatchEntity")
@Table(name = "accounting_core_transaction_batch")
@NoArgsConstructor
public class TransactionBatchEntity {

    @Id
    @Column(name = "transaction_batch_id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Embedded
    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "filtering_parameters_organisation_id")),
            @AttributeOverride(name = "from", column = @Column(name = "filtering_parameters_from_date")),
            @AttributeOverride(name = "to", column = @Column(name = "filtering_parameters_to_date")),
            @AttributeOverride(name = "transactionNumber", column = @Column(name = "filtering_parameters_transaction_number")),
    })
    private FilteringParameters filteringParameters;

    @NotBlank
    @Column(name = "issued_by", nullable = false)
    private String issuedBy;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    @OneToMany(mappedBy = "transactionBatch", orphanRemoval = true)
    private Set<TransactionEntity> transactions = new LinkedHashSet<>();

    @NotNull
    private BatchStatus status;

}
