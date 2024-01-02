package org.cardanofoundation.lob.module.netsuite.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "netsuite_ingestion")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetSuiteIngestion extends AbstractTimestampEntity {

    @Id
//    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "ingestion_body", nullable = false, length = 999_999, columnDefinition = "TEXT")
    private String ingestionBody;

    @Column(name = "ingestion_body_checksum", nullable = false)
    private String ingestionBodyChecksum;

}
