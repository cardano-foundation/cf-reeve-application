package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionEntity")
@Table(name = "blockchain_publisher_transaction")
@NoArgsConstructor
@Builder
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_internal_number", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "base_currency_internal_code", nullable = false)
    private String baseCurrencyInternalCode;

    @Column(name = "base_currency_id", nullable = false)
    private String baseCurrencyId;

    @Column(name = "target_currency_internal_code", nullable = false)
    private String targetCurrencyInternalCode;

    @Column(name = "target_currency_id", nullable = false)
    private String targetCurrencyId;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @Column(name = "publish_status", nullable = false)
    @Enumerated(STRING)
    private BlockchainPublishStatus publishStatus;

    @Nullable
    @Column(name = "l1_transaction_hash")
    private String l1TransactionHash;

    @Nullable
    @Column(name = "l1_absolute_slot")
    private Long l1AbsoluteSlot;

    @Nullable
    @Column(name = "l1_assurance_level")
    @Enumerated(STRING)
    private OnChainAssuranceLevel l1AssuranceLevel;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, cascade = ALL, fetch = EAGER)
    @Builder.Default
    private Set<TransactionItemEntity> lines = new LinkedHashSet<>();

    public Optional<OnChainAssuranceLevel> getOnChainAssuranceLevel() {
        return Optional.ofNullable(l1AssuranceLevel);
    }

    public Optional<String> getL1TransactionHash() {
        return Optional.ofNullable(l1TransactionHash);
    }

    public Optional<Long> getL1AbsoluteSlot() {
        return Optional.ofNullable(l1AbsoluteSlot);
    }

    public Optional<OnChainAssuranceLevel> getL1AssuranceLevel() {
        return Optional.ofNullable(l1AssuranceLevel);
    }

    public Optional<DocumentEntity> getDocument() {
        return Optional.ofNullable(document);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof TransactionEntity te) {
            return te.getId().equals(id);
        }

        return false;
    }

}
