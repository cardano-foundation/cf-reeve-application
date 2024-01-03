package org.cardanofoundation.lob.app.netsuite.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners({AuditingEntityListener.class})
@Table(name = "netsuite_ingestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class NetSuiteIngestion extends BaseEntity {

    @Column(name = "ingestion_body", nullable = false, length = 999_999, columnDefinition = "TEXT")
    private String ingestionBody;

    @Column(name = "ingestion_body_checksum", nullable = false)
    private String ingestionBodyChecksum;

}
