package org.cardanofoundation.lob.app.netsuite.repository;

import org.cardanofoundation.lob.app.netsuite.domain.entity.NetSuiteIngestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRepository extends JpaRepository<NetSuiteIngestion, String> {

}
