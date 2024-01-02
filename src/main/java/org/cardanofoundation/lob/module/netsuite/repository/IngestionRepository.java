package org.cardanofoundation.lob.module.netsuite.repository;

import org.cardanofoundation.lob.module.netsuite.domain.entity.NetSuiteIngestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRepository extends JpaRepository<NetSuiteIngestion, String> {

}
