package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionItemEntity")
@Table(name = "blockchain_publisher_transaction_item")
@NoArgsConstructor
@Builder
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionItemEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_item_id", nullable = false)
    private String id;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Nullable
    @Column(name = "event_code")
    private String eventCode;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "project_customer_code"))
    })
    private Project project;

    @Embedded
    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "cost_center_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "cost_center_name"))
    })
    private CostCenter costCenter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "num", column = @Column(name = "document_num")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),
            @AttributeOverride(name = "currency.id", column = @Column(name = "document_currency_id")),
            @AttributeOverride(name = "counterparty.customerCode", column = @Column(name = "document_counterparty_customer_code")),
            @AttributeOverride(name = "counterparty.type", column = @Column(name = "document_counterparty_type")),
    })
    private Document document;

    public Optional<String> getEventCode() {
        return Optional.ofNullable(eventCode);
    }

    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    public Optional<CostCenter> getCostCenter() {
        return Optional.ofNullable(costCenter);
    }

}
