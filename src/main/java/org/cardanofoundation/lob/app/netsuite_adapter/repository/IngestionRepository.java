package org.cardanofoundation.lob.app.netsuite_adapter.repository;

import org.cardanofoundation.lob.app.netsuite_adapter.domain.entity.NetSuiteIngestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionRepository extends JpaRepository<NetSuiteIngestion, String> {

}
