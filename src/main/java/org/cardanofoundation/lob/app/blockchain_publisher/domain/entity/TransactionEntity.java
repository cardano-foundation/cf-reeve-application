package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
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
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "internal_number", nullable = false)
    private String internalNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "shortName", column = @Column(name = "organisation_short_name")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "organisation_currency_id")),
            @AttributeOverride(name = "currency.externalNumber", column = @Column(name = "organisation_currency_internal_number"))
    })
    private Organisation organisation;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Embedded
    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "cost_center_code"))
    })
    private CostCenter costCenter;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "internalNumber", column = @Column(name = "project_internal_number")),
            @AttributeOverride(name = "code", column = @Column(name = "project_code"))
    })
    private Project project;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "internalDocumentNumber", column = @Column(name = "document_internal_document_number")),
            //@AttributeOverride(code = "vat.internalNumber", column = @Column(code = "document_vat_internal_number")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),
            @AttributeOverride(name = "counterparty.internalNumber", column = @Column(name = "document_counterparty_internal_number")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "document_currency_id")),
            //@AttributeOverride(code = "currency.internalNumber", column = @Column(code = "document_currency_internal_number")),
    })
    private Document document;

    @Nullable
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "transactionHash", column = @Column(name = "l1_transaction_hash")),
            @AttributeOverride(name = "absoluteSlot", column = @Column(name = "l1_absolute_slot")),
            @AttributeOverride(name = "creationSlot", column = @Column(name = "l1_creation_slot")),
            @AttributeOverride(name = "assuranceLevel", column = @Column(name = "l1_assurance_level")),
            @AttributeOverride(name = "publishStatus", column = @Column(name = "l1_publish_status"))
    })
    private L1SubmissionData l1SubmissionData;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, cascade = ALL, fetch = EAGER)
    @Builder.Default
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

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

    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    public Optional<CostCenter> getCostCenter() {
        return Optional.ofNullable(costCenter);
    }

    public Optional<L1SubmissionData> getL1SubmissionData() {
        return Optional.ofNullable(l1SubmissionData);
    }

}
