package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionBatchAssocEntity")
@Table(name = "accounting_core_transaction_batch_assoc")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionBatchAssocEntity extends AuditEntity implements Persistable<TransactionBatchAssocEntity.Id> {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "transactionBatchId", column = @Column(name = "transaction_batch_id")),
            @AttributeOverride(name = "transactionId", column = @Column(name = "transaction_id"))
    })
    private Id id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionBatchAssocEntity that = (TransactionBatchAssocEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public Id getId() {
        return id;
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class Id {

        private String transactionBatchId;
        private String transactionId;

    }

}
