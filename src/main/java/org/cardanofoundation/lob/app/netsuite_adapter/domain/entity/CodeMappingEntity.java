package org.cardanofoundation.lob.app.netsuite_adapter.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.lob.app.support.audit_support.AuditEntity;

import java.io.Serializable;

@Getter
@Setter
@Entity(name = "netsuite.CodeMappingEntity")
@Table(name = "netsuite_code_mapping")
@NoArgsConstructor
public class CodeMappingEntity extends AuditEntity {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "mappingId", column = @Column(name = "mapping_id")),
            @AttributeOverride(name = "internalId", column = @Column(name = "internal_id")),
            @AttributeOverride(name = "type", column = @Column(name = "code_type"))
    })
    private Id id;

    @Column(name = "customerCode")
    private String code;

    @Embeddable
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Serializable {

        private String mappingId;

        private Long internalId;

        @Enumerated(EnumType.STRING)
        private CodeMappingType type;

    }
}
