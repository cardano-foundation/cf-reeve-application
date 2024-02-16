package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionEntity")
@Table(name = "accounting_core_transaction")
@NoArgsConstructor
@ToString
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "organisation_currency_id")),
            @AttributeOverride(name = "currency.internalNumber", column = @Column(name = "organisation_currency_internal_number"))
    })
    private Organisation organisation;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "internalNumber", column = @Column(name = "document_internal_number")),

            @AttributeOverride(name = "currency.id", column = @Column(name = "document_currency_id")),
            @AttributeOverride(name = "currency.internalNumber", column = @Column(name = "document_currency_internal_number")),

            @AttributeOverride(name = "vat.internalNumber", column = @Column(name = "document_vat_internal_number")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),

            @AttributeOverride(name = "counterparty.internalNumber", column = @Column(name = "document_counterparty_internal_number")),
            @AttributeOverride(name = "counterparty.name", column = @Column(name = "document_counterparty_name")),
    })
    private Document document;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Nullable
    @Column(name = "cost_center_internal_number")
    private String costCenterInternalNumber;

    @Nullable
    @Column(name = "project_internal_number")
    private String projectInternalNumber;

    @Column(name = "validation_status", nullable = false)
    @Enumerated(STRING)
    private ValidationStatus validationStatus;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    private Boolean ledgerDispatchApproved;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true, cascade = ALL, fetch = EAGER)
    private Set<TransactionItemEntity> items = new LinkedHashSet<>();

    public Optional<String> getCostCenterInternalNumber() {
        return Optional.ofNullable(costCenterInternalNumber);
    }

    public Optional<String> getProjectInternalNumber() {
        return Optional.ofNullable(projectInternalNumber);
    }

}
