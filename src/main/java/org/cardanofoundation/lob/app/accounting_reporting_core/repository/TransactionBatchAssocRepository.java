package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionBatchAssocRepository extends JpaRepository<TransactionBatchAssocEntity, TransactionBatchAssocEntity.Id> {

}
