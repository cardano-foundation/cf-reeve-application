package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;

@Accessors(fluent = true)
@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionEntity")
@Table(name = "accounting_core_transaction")
@NoArgsConstructor
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
            @AttributeOverride(name = "currency.id", column = @Column(name = "organisation_currency_id")),
            @AttributeOverride(name = "currency.customerCode", column = @Column(name = "organisation_currency_customer_code"))
    })
    private Organisation organisation;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Column(name = "validation_status", nullable = false)
    @Enumerated(STRING)
    private ValidationStatus validationStatus;

    @Column(name = "transaction_approved", nullable = false)
    private Boolean transactionApproved;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    private Boolean ledgerDispatchApproved;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, cascade = ALL, fetch = EAGER)
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

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
        return createdAt == null;
    }

}
