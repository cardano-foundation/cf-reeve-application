package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionBatchRepository extends JpaRepository<TransactionBatchEntity, String> {
}
