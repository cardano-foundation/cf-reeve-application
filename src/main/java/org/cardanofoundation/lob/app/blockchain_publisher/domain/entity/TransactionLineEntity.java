package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionLineEntity")
@Table(name = "blockchain_publisher_transaction_line")
@NoArgsConstructor
@ToString
public class TransactionLineEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "organisation_id", nullable = false)
    private String organisationId;

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(STRING)
    private TransactionType transactionType;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "transaction_internal_number", nullable = false)
    private String transactionInternalNumber;

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

    @Nullable
    @Column(name = "document_internal_number")
    private String documentInternalNumber;

    @Nullable
    @Column(name = "vendor_internal_code")
    private String vendorInternalCode;

    @Nullable
    @Column(name = "vat_internal_code")
    private String vatInternalCode;

    @Nullable
    @Column(name = "vat_rate")
    private BigDecimal vatRate;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Column(name = "amount_lcy", nullable = false)
    private BigDecimal amountLcy;

    @Column(name = "publish_status", nullable = false)
    @Enumerated(STRING)
    private BlockchainPublishStatus publishStatus;

    @Nullable
    @Column(name = "assurance_level")
    @Enumerated(STRING)
    private OnChainAssuranceLevel assuranceLevel;

    public Optional<OnChainAssuranceLevel> getOnChainAssuranceLevel() {
        return Optional.ofNullable(assuranceLevel);
    }

    public Optional<BigDecimal> getFxRate() {
        return Optional.ofNullable(fxRate);
    }

}
