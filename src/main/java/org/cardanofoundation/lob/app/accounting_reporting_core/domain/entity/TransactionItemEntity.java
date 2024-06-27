package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import com.google.common.base.Objects;
import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPSourceVersionRelevant;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionItemEntity")
@Table(name = "accounting_core_transaction_item")
@NoArgsConstructor
@ToString
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionItemEntity extends AuditEntity implements Persistable<String> {

    @Id
    @Column(name = "transaction_item_id", nullable = false)
    @LOB_ERPSourceVersionRelevant
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "account_code_debit")),
            @AttributeOverride(name = "refCode", column = @Column(name = "account_ref_code_debit")),
            @AttributeOverride(name = "name", column = @Column(name = "account_name_debit"))
    })
    @Nullable
    @LOB_ERPSourceVersionRelevant
    private Account accountDebit;

    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "account_code_credit")),
            @AttributeOverride(name = "refCode", column = @Column(name = "account_ref_code_credit")),
            @AttributeOverride(name = "name", column = @Column(name = "account_name_credit"))
    })
    @Nullable
    @LOB_ERPSourceVersionRelevant
    private Account accountCredit;

    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "account_event_code")),
            @AttributeOverride(name = "name", column = @Column(name = "account_event_name")),
    })
    private AccountEvent accountEvent;

    @Column(name = "amount_fcy", nullable = false)
    @LOB_ERPSourceVersionRelevant
    private BigDecimal amountFcy;

    @Column(name = "amount_lcy", nullable = false)
    @LOB_ERPSourceVersionRelevant
    private BigDecimal amountLcy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rejectionCode", column = @Column(name = "rejection_code")),
    })
    @Nullable
    private Rejection rejection;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "fx_rate", nullable = false)
    @LOB_ERPSourceVersionRelevant
    private BigDecimal fxRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "project_customer_code")),
            @AttributeOverride(name = "externalCustomerCode", column = @Column(name = "project_external_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "project_name"))
    })
    @Nullable
    @LOB_ERPSourceVersionRelevant
    private Project project;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "customerCode", column = @Column(name = "cost_center_customer_code")),
            @AttributeOverride(name = "externalCustomerCode", column = @Column(name = "cost_center_external_customer_code")),
            @AttributeOverride(name = "name", column = @Column(name = "cost_center_name"))
    })
    @Nullable
    @LOB_ERPSourceVersionRelevant
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
    @LOB_ERPSourceVersionRelevant
    private Document document;

    public void clearAccountCodeCredit() {
        this.accountCredit = null;
    }

    public void clearAccountCodeDebit() {
        this.accountDebit = null;
    }

    public Optional<Account> getAccountDebit() {
        return Optional.ofNullable(accountDebit);
    }

    public Optional<Account> getAccountCredit() {
        return Optional.ofNullable(accountCredit);
    }

    public Optional<AccountEvent> getAccountEvent() {
        return Optional.ofNullable(accountEvent);
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

    public Optional<Rejection> getRejection() {
        return Optional.ofNullable(rejection);
    }

    public Optional<OperationType> getOperationType() {
        val amountLcy = this.amountLcy.intValue();

        if (amountLcy == 0) return Optional.empty();

        return amountLcy < 0 ? Optional.of(OperationType.CREDIT) : Optional.of(OperationType.DEBIT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        val that = (TransactionItemEntity) o;

        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
