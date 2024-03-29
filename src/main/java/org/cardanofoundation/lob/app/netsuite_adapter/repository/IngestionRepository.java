package org.cardanofoundation.lob.app.netsuite_adapter.repository;

import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRepository extends JpaRepository<NetSuiteIngestionEntity, String> {

}
