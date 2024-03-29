package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Accessors(fluent = true)
@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionItemEntity")
@Table(name = "accounting_core_transaction_item")
@NoArgsConstructor
@ToString
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionItemEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_item_id", nullable = false)
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }

    @Nullable
    @Column(name = "account_code_debit")
    private String accountCodeDebit;

    @Nullable
    @Column(name = "account_code_ref_debit")
    private String accountCodeRefDebit;

    @Nullable
    @Column(name = "account_code_credit")
    private String accountCodeCredit;

    @Nullable
    @Column(name = "account_code_ref_credit")
    private String accountCodeRefCredit;

    @Nullable
    @Column(name = "account_name_debit")
    private String accountNameDebit;

    @Nullable
    @Column(name = "account_event_code")
    private String accountEventCode;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Column(name = "amount_lcy", nullable = false)
    private BigDecimal amountLcy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "project_customer_code"))
    })
    @Nullable
    private Project project;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "cost_center_customer_code")),
            @AttributeOverride(name = "externalCustomerCode", column = @Column(name = "cost_center_external_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "cost_center_name"))
    })
    @Nullable
    private CostCenter costCenter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "num", column = @Column(name = "document_num")),

            @AttributeOverride(name = "currency.id", column = @Column(name = "document_currency_id")),
            @AttributeOverride(name = "currency.customerCode", column = @Column(name = "document_currency_customer_code")),

            @AttributeOverride(name = "vat.customerCode", column = @Column(name = "document_vat_customer_code")),
            @AttributeOverride(name = "vat.rate", column = @Column(name = "document_vat_rate")),

            @AttributeOverride(name = "counterparty.customerCode", column = @Column(name = "document_counterparty_customer_code")),
            @AttributeOverride(name = "counterparty.type", column = @Column(name = "document_counterparty_type")),
            @AttributeOverride(name = "counterparty.name", column = @Column(name = "document_counterparty_name")),
    })
    @Nullable
    private Document document;

    public Optional<String> getAccountCodeDebit() {
        return Optional.ofNullable(accountCodeDebit);
    }

    public Optional<String> getAccountNameDebit() {
        return Optional.ofNullable(accountNameDebit);
    }

    public Optional<String> getAccountCodeCredit() {
        return Optional.ofNullable(accountCodeCredit);
    }

    public Optional<String> getAccountCodeRefDebit() {
        return Optional.ofNullable(accountCodeRefDebit);
    }

    public Optional<String> getAccountCodeRefCredit() {
        return Optional.ofNullable(accountCodeRefCredit);
    }

    public Optional<String> getAccountEventCode() {
        return Optional.ofNullable(accountEventCode);
    }

    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    public Optional<CostCenter> getCostCenter() {
        return Optional.ofNullable(costCenter);
    }

    public Optional<Document> getDocument() {
        return Optional.ofNullable(document);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionItemEntity that = (TransactionItemEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
