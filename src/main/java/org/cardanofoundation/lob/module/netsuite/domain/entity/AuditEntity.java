package org.cardanofoundation.lob.module.netsuite.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.TemporalType.TIMESTAMP;

@Setter
@Getter
@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_by")
    @CreatedBy
    protected String createdBy = "system";

    @Column(name = "updated_by")
    @LastModifiedBy
    protected String updatedBy = "system";

    @Temporal(TIMESTAMP)
    @Column(name = "created_at")
    @CreatedDate
    protected LocalDateTime createdAt = LocalDateTime.now();

    @Temporal(TIMESTAMP)
    @Column(name = "updated_at")
    @LastModifiedDate
    protected LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
