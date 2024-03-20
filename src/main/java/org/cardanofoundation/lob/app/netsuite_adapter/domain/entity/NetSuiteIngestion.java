package org.cardanofoundation.lob.app.netsuite_adapter.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;

@Entity
@Table(name = "netsuite_ingestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetSuiteIngestion extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    @Column(name = "ingestion_body", nullable = false, length = 999_999, columnDefinition = "TEXT")
    private String ingestionBody;

    @Column(name = "ingestion_body_debug", nullable = false, length = 999_999, columnDefinition = "TEXT")
    private String ingestionBodyDebug;

    @Column(name = "ingestion_body_checksum", nullable = false)
    private String ingestionBodyChecksum;

}
