package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Entity
@Table(name = "accounting_core_transaction_violation")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ViolationEntity extends AuditEntity implements Persistable<ViolationEntity.Id> {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "transactionId", column = @Column(name = "transaction_id", nullable = false)),
            @AttributeOverride(name = "txItemId", column = @Column(name = "tx_item_id", nullable = false)),
            @AttributeOverride(name = "code", column = @Column(name = "code", nullable = false)),
    })
    @NotNull
    private ViolationEntity.Id id;

    @NotNull
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "transaction_id", updatable = false, insertable = false, nullable = false)
    private TransactionEntity transaction;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "type")
    private Violation.Type type;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "source")
    private Violation.Source source;

    @NotBlank
    @Column(name = "processor_module")
    private String processorModule;

    @Builder.Default
    @Column(name = "bag")
    @Type(value = io.hypersistence.utils.hibernate.type.json.JsonType.class)
    private Map<String, Object> bag = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViolationEntity that = (ViolationEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }

    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Id {

        @NotNull
        @Getter
        private String transactionId;

        @NotNull
        private String txItemId = ""; // TODO try to make it null and see if it works (last time it didn't work)

        @NotNull
        @Enumerated(STRING)
        @Getter
        private Violation.Code code;

        public Id create(String transactionId,
                         Optional<String> txItemId,
                         Violation.Code code) {
            return new Id(transactionId, txItemId.orElse(""), code);
        }

        public Optional<String> getTxItemId() {
            if (txItemId.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(txItemId);
        }
    }

}
