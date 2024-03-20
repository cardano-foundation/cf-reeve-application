package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;

import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@Accessors(fluent = true)
@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionBatchEntity")
@Table(name = "accounting_core_transaction_batch")
@NoArgsConstructor
public class TransactionBatchEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_batch_id", nullable = false)
    @NotNull
    private String id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "filtering_parameters_organisation_id")),
            @AttributeOverride(name = "from", column = @Column(name = "filtering_parameters_from_date")),
            @AttributeOverride(name = "to", column = @Column(name = "filtering_parameters_to_date")),
            @AttributeOverride(name = "transactionNumber", column = @Column(name = "filtering_parameters_transaction_number")),
    })
    @NotNull
    private FilteringParameters filteringParameters;

    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "accounting_core_transaction_batch_assoc",
            joinColumns = @JoinColumn(name = "transaction_batch_id"),
            inverseJoinColumns = @JoinColumn(name = "transaction_id"))
    @NotNull
    private Set<TransactionEntity> transactions = new LinkedHashSet<>();

//    @NotNull
//    @Enumerated(STRING)
//    @Column(name = "status", nullable = false)
//    private TransactionBatchStatus status = TransactionBatchStatus.CREATED;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionBatchEntity that = (TransactionBatchEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return STR."TransactionBatchEntity{id='\{id}\{'\''}, createdBy='\{createdBy}\{'\''}, updatedBy='\{updatedBy}\{'\''}, createdAt=\{createdAt}, updatedAt=\{updatedAt}\{'}'}";
    }

}
