package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountingCoreRepository extends JpaRepository<TransactionLine, UUID> {

}
