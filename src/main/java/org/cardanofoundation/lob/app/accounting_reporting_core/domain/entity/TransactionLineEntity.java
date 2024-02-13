package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionLineEntity")
@Table(name = "accounting_core_transaction_line")
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Audited
@EntityListeners({AuditingEntityListener.class})
public class TransactionLineEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @Column(name = "base_currency_internal_code", nullable = false)
    private String baseCurrencyInternalCode;

    @Column(name = "base_currency_id", nullable = false)
    private String baseCurrencyId;

    @Column(name = "target_currency_internal_code", nullable = false)
    private String targetCurrencyInternalCode;

    @Column(name = "target_currency_id")
    @Nullable
    private String targetCurrencyId;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Nullable
    @Column(name = "document_internal_number")
    private String documentInternalNumber;

    @Nullable
    @Column(name = "counterparty_internal_code")
    private String counterpartyInternalCode;

    @Nullable
    @Column(name = "counterparty_name")
    private String counterpartyName;

    @Nullable
    @Column(name = "cost_center_internal_code")
    private String costCenterInternalCode;

    @Nullable
    @Column(name = "project_internal_code")
    private String projectInternalCode;

    @Nullable
    @Column(name = "vat_internal_code")
    private String vatInternalCode;

    @Nullable
    @Column(name = "vat_rate")
    private BigDecimal vatRate;

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

}
