package org.cardanofoundation.lob.app.netsuite_adapter.repository;

import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.CodeMappingEntity;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.CodeMappingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CodesMappingRepository extends JpaRepository<CodeMappingEntity, CodeMappingEntity.Id> {

}
