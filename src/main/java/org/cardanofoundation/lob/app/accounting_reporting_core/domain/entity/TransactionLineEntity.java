package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity
@Table(name = "accounting_core_transaction_line")
@NoArgsConstructor
@ToString
public class TransactionLineEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

    @Column(name = "account_code_debit", nullable = false)
    private String accountCodeDebit;

    @Column(name = "ledger_dispatch_status", nullable = false)
    @Enumerated(STRING)
    org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus ledgerDispatchStatus = TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "base_currency_id")),
            @AttributeOverride(name = "internalCode", column = @Column(name = "base_currency_internal_code"))
    })
    private Currency baseCurrency;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "target_currency_id")),
            @AttributeOverride(name = "internalCode", column = @Column(name = "target_currency_internal_code"))
    })
    private Currency targetCurrency;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Column(name = "ingestion_id", nullable = false)
    private UUID ingestionID;

    @Nullable
    @Column(name = "document_internal_number")
    private String documentInternalNumber;

    @Nullable
    @Column(name = "vendor_internal_code")
    private String vendorInternalCode;

    @Nullable
    @Column(name = "vendor_name")
    private String vendorName;

    @Nullable
    @Column(name = "cost_center_internal_code")
    private String costCenterInternalCode;

    @Nullable
    @Column(name = "project_internal_code")
    private String projectInternalCode;

    @Nullable
    @Embedded
    private Vat vat;

    @Nullable
    @Column(name = "account_name_debit")
    private String accountNameDebit;

    @Nullable
    @Column(name = "account_credit")
    private String accountCredit;

    @Nullable
    @Column(name = "validated")
    private Boolean validated;

    @Nullable
    @Column(name = "amount_fcy")
    BigDecimal amountFcy;

    @Nullable
    @Column(name = "amount_lcy")
    BigDecimal amountLcy;

}
