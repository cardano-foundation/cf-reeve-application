package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity(name = "blockchain_publisher.TransactionLineEntity")
@Table(name = "blockchain_publisher_transaction_line")
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class TransactionLineEntity extends AuditEntity {

    @Id
    @Column(name = "transaction_line_id", nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

    @Column(name = "amount_fcy", nullable = false)
    private BigDecimal amountFcy;

    @Column(name = "amount_lcy", nullable = false)
    private BigDecimal amountLcy;

}
