package org.cardanofoundation.lob.app.netsuite_adapter.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationAwareInternalId implements Serializable {

    private String organisationId;

    private Long internalId;

}
