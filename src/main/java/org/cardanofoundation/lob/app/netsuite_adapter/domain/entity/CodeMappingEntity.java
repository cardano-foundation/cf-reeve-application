package org.cardanofoundation.lob.app.netsuite_adapter.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "netsuite.CodeMappingEntity")
@Table(name = "netsuite_code_mapping")
@NoArgsConstructor
public class CodeMappingEntity {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id")),
            @AttributeOverride(name = "internalId", column = @Column(name = "internal_id"))
    })
    private OrganisationAwareInternalId id;

    @Column(name = "code_type")
    @Enumerated(EnumType.STRING)
    private CodeMappingType type;

    @Column(name = "code")
    private String code;

}
