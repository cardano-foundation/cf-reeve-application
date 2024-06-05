package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.support.audit.AuditEntity;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "netsuite_adapter_ingestion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetSuiteIngestionEntity extends AuditEntity implements Persistable<String> {

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

    @Override
    public String getId() {
        return id;
    }

}
