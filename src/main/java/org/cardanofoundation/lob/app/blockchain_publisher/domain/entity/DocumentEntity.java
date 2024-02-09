package org.cardanofoundation.lob.app.blockchain_publisher.domain.entity;


import jakarta.persistence.*;
import lombok.*;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@Setter
@Entity(name = "blockchain_publisher.DocumentEntity")
@Table(name = "blockchain_publisher_document")
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
//@Audited
//@EntityListeners({AuditingEntityListener.class})
public class DocumentEntity extends AuditEntity {

    @Id
    @Column(name = "internal_document_number", nullable = false)
    private String id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "internalCode", column = @Column(name = "vat_internal_code")),
            @AttributeOverride(name = "rate", column = @Column(name = "vat_rate")),
    })
    @Nullable
    private Vat vat;

    @Nullable
    @Column(name = "vendor_internal_code")
    private String vendorInternalCode;

    @OneToOne(mappedBy = "document", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private TransactionEntity transaction;

    public Optional<Vat> getVat() {
        return Optional.ofNullable(vat);
    }

    public Optional<String> getVendorInternalCode() {
        return Optional.ofNullable(vendorInternalCode);
    }

}
