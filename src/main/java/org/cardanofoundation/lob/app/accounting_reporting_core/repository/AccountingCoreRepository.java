package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountingCoreRepository extends JpaRepository<TransactionLineEntity, String> {

    @Query("SELECT tl FROM TransactionLineEntity tl WHERE tl.organisationId = :organisationId AND tl.ledgerDispatchStatus in :dispatchStatuses")
    List<TransactionLineEntity> findByPendingTransactionLinesByOrganisationAndDispatchStatus(@Param("organisationId") String organisationId,
                                                                                             @Param("dispatchStatuses") List<TransactionLine.LedgerDispatchStatus> dispatchStatuses);

}

