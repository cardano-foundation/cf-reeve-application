package org.cardanofoundation.lob.app.adapter.netsuite.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity extends AuditEntity {

    @Id
    @GeneratedValue(generator = "netsuite_ingestion_gen")
    @SequenceGenerator(name = "netsuite_ingestion_gen", sequenceName = "netsuite_ingestion_seq", initialValue = 1, allocationSize = 1)
    @Column(name = "id", nullable = false)
    protected Long id;

}
