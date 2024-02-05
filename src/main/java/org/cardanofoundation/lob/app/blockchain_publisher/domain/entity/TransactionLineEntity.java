package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.AuditEntity;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@Audited
@EntityListeners({AuditingEntityListener.class})
public class TransactionLineEntity extends AuditEntity {

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
    @Column(name = "l1_transaction_hash")
    private String l1TransactionHash;

    @Nullable
    @Column(name = "l1_absolute_slot")
    private Long l1AbsoluteSlot;

    @Nullable
    @Column(name = "l1_assurance_level")
    @Enumerated(STRING)
    private OnChainAssuranceLevel assuranceLevel;

    public Optional<OnChainAssuranceLevel> getOnChainAssuranceLevel() {
        return Optional.ofNullable(assuranceLevel);
    }

    public Optional<String> getVendorInternalCode() {
        return Optional.ofNullable(vendorInternalCode);
    }

    public Optional<String> getL1TransactionHash() {
        return Optional.ofNullable(l1TransactionHash);
    }

    public Optional<Long> getL1AbsoluteSlot() {
        return Optional.ofNullable(l1AbsoluteSlot);
    }

    public Optional<OnChainAssuranceLevel> getAssuranceLevel() {
        return Optional.ofNullable(assuranceLevel);
    }

    public Optional<String> getVatInternalCode() {
        return Optional.ofNullable(vatInternalCode);
    }

    public Optional<BigDecimal> getVatRate() {
        return Optional.ofNullable(vatRate);
    }

    public Optional<String> getDocumentInternalNumber() {
        return Optional.ofNullable(documentInternalNumber);
    }

    public Optional<String> getTransactionInternalNumber() {
        return Optional.ofNullable(transactionInternalNumber);
    }

}
