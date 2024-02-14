package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionLineEntity")
@Table(name = "accounting_core_transaction_line")
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionLineEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private String id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "organisation_currency_id")),
            @AttributeOverride(name = "currency.internalCode", column = @Column(name = "organisation_currency_internal_code"))
    })
    private Organisation organisation;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

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
            @AttributeOverride(name = "currency.internalCode", column = @Column(name = "document_currency_internal_code")),

            @AttributeOverride(name = "vat.internalCode", column = @Column(name = "document_vat_internal_code")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),

            @AttributeOverride(name = "counterparty.internalCode", column = @Column(name = "document_counterparty_internal_code")),
            @AttributeOverride(name = "counterparty.name", column = @Column(name = "document_counterparty_name")),
    })
    private Document document;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Nullable
    @Column(name = "cost_center_internal_code")
    private String costCenterInternalCode;

    @Nullable
    @Column(name = "project_internal_code")
    private String projectInternalCode;

    @Nullable
    @Column(name = "account_code_debit")
    private String accountCodeDebit;

    @Nullable
    @Column(name = "account_name_debit")
    private String accountNameDebit;

    @Nullable
    @Column(name = "account_code_credit")
    private String accountCodeCredit;

    @Column(name = "validation_status", nullable = false)
    @Enumerated(STRING)
    private ValidationStatus validationStatus;

    @Column(name = "amount_fcy", nullable = false)
    BigDecimal amountFcy;

    @Column(name = "amount_lcy", nullable = false)
    BigDecimal amountLcy;

    @Column(name = "ledger_dispatch_approved", nullable = false)
    Boolean ledgerDispatchApproved;

    public Optional<String> getCostCenterInternalCode() {
        return Optional.ofNullable(costCenterInternalCode);
    }

    public Optional<String> getProjectInternalCode() {
        return Optional.ofNullable(projectInternalCode);
    }

    public Optional<String> getAccountCodeDebit() {
        return Optional.ofNullable(accountCodeDebit);
    }

    public Optional<String> getAccountNameDebit() {
        return Optional.ofNullable(accountNameDebit);
    }

    public Optional<String> getAccountCodeCredit() {
        return Optional.ofNullable(accountCodeCredit);
    }

}
