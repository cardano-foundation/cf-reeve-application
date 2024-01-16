package org.cardanofoundation.lob.app.netsuite_adapter.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
//@EntityListeners({AuditingEntityListener.class})
@Table(name = "netsuite_ingestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Audited
public class NetSuiteIngestion extends AuditEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    protected UUID id;

    @Column(name = "ingestion_body", nullable = false, length = 999_999, columnDefinition = "TEXT")
    private String ingestionBody;

    @Column(name = "ingestion_body_checksum", nullable = false)
    private String ingestionBodyChecksum;

}
