package org.cardanofoundation.lob.app.netsuite_adapter.repository;

import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.OrganisationAwareInternalId;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.CodeMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodesMappingRepository extends JpaRepository<CodeMappingEntity, OrganisationAwareInternalId> {

}
