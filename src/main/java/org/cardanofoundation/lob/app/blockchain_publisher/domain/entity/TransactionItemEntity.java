package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import java.math.BigDecimal;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Column(name = "event_code")
    private BigDecimal eventCode;

}
