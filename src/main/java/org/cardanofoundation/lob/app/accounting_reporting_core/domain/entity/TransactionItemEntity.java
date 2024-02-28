package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;

@Getter
@Setter
@Entity(name = "accounting_reporting_core.TransactionItemEntity")
@Table(name = "accounting_core_transaction_item")
@NoArgsConstructor
@ToString
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionItemEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_item_id", nullable = false)
    private String id;

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

}
