package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "accounting_core_transaction_line")
@NoArgsConstructor
public class TransactionLine {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "transaction_number", nullable = false)
    private String transactionNumber;

    @Column(name = "account_code_debit", nullable = false)
    private String accountCodeDebit;

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Nullable
    @Column(name = "document_number")
    private String documentNumber;

    @Nullable
    @Column(name = "vendor_code")
    private String vendorCode;

    @Nullable
    @Column(name = "vendor_name")
    private String vendorName;

    @Nullable
    @Column(name = "cost_center")
    private String costCenter;

    @Nullable
    @Column(name = "project_code")
    private String projectCode;

    @Nullable
    @Column(name = "vat_code")
    private String vatCode;

    @Nullable
    @Column(name = "account_name_debit")
    private String accountNameDebit;

    @Nullable
    @Column(name = "account_credit")
    private String accountCredit;

    @Nullable
    @Column(name = "memo")
    String memo;

    @Nullable
    @Column(name = "amount_fcy")
    BigDecimal amountFcy;

    @Nullable
    @Column(name = "amount_lcy")
    BigDecimal amountLcy;

}
