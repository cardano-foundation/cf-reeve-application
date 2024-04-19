package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.RejectionStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.RejectionStatus.NOT_REJECTED;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionEntity")
@Table(name = "accounting_core_transaction")
@NoArgsConstructor
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "accounting_period", nullable = false)
    private YearMonth accountingPeriod;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    private LedgerDispatchStatus ledgerDispatchStatus = NOT_DISPATCHED;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "shortName", column = @Column(name = "organisation_short_name")),
            @AttributeOverride(name = "currencyId", column = @Column(name = "organisation_currency_id")),
    })
    private Organisation organisation;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Column(name = "validation_status", nullable = false)
    @Enumerated(STRING)
    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;

    @Column(name = "transaction_approved", nullable = false)
    private Boolean transactionApproved = false;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    private Boolean ledgerDispatchApproved = false;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, fetch = EAGER)
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

    @Column(name = "user_comment")
    private String userComment;

    @Column(name = "rejection_status", nullable = false)
    @Enumerated(STRING)
    private RejectionStatus rejectionStatus = NOT_REJECTED;

    @ElementCollection
    @CollectionTable(name = "accounting_core_transaction_violation", joinColumns = @JoinColumn(name = "transaction_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "code", nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "type", nullable = false)),
            @AttributeOverride(name = "txItemId", column = @Column(name = "tx_item_id", nullable = false)),
            @AttributeOverride(name = "source", column = @Column(name = "source", nullable = false)),
            @AttributeOverride(name = "processorModule", column = @Column(name = "processor_module", nullable = false)),
            @AttributeOverride(name = "bag", column = @Column(name = "bag", nullable = false))
    })
    private Set<Violation> violations = new LinkedHashSet<>();

    public boolean allApprovalsPassedForTransactionDispatch() {
        return transactionApproved && ledgerDispatchApproved;
    }

    public void addViolation(Violation violation) {
        this.violations.add(violation);

        recalcValidationStatus();
    }

    public void clearAllViolations() {
        violations.clear();
        recalcValidationStatus();
    }

    private void recalcValidationStatus() {
        validationStatus = violations.isEmpty() ? ValidationStatus.VALIDATED : ValidationStatus.FAILED;
    }

    public boolean isTheSameBusinessWise(TransactionEntity other) {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.id, other.getId());
        equalsBuilder.append(this.entryDate, other.entryDate);
        equalsBuilder.append(this.transactionType, other.transactionType);
        equalsBuilder.append(this.organisation, other.organisation);
        equalsBuilder.append(this.fxRate, other.fxRate);
        equalsBuilder.append(this.accountingPeriod, other.accountingPeriod);
        equalsBuilder.append(this.transactionInternalNumber, other.transactionInternalNumber);
        equalsBuilder.append(this.validationStatus, other.validationStatus);

        val rootMatch = equalsBuilder.isEquals();

        // Compare items only if all other fields are equal
        if (rootMatch) {
            // Ensure both sets are of the same size
            if (this.items.size() != other.items.size()) {
                return false;
            }

            return this.items.stream()
                    .allMatch(thisItem -> other.items.stream()
                            .anyMatch(thisItem::isTheSameBusinessWise));
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionEntity that = (TransactionEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return STR."TransactionEntity{id='\{id}\{'\''}, transactionInternalNumber='\{transactionInternalNumber}\{'\''}, batchId='\{batchId}'}";
    }

}
