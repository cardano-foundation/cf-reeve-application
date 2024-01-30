package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountingCoreRepository extends JpaRepository<TransactionLineEntity, String> {

    @Query("SELECT tl.id FROM TransactionLineEntity tl WHERE tl.organisationId = :organisationId AND tl.id in :txLineIds AND tl.ledgerDispatchStatus IN ('NOT_DISPATCHED', 'FAILED')")
    List<String> findNotYetDispatchedAndFailedTxLineIds(@Param("organisationId") String organisationId,
                                                        @Param("txLineIds") List<String> txLineIds);

    @Query("SELECT tl.id FROM TransactionLineEntity tl WHERE tl.organisationId = :organisationId AND tl.id in :txLineIds AND tl.ledgerDispatchStatus IN ('DISPATCHED', 'COMPLETED', 'FINALISED')")
    List<String> findDoneTxLineIds(@Param("organisationId") String organisationId,
                                                        @Param("txLineIds") List<String> txLineIds);

    @Query("SELECT tl FROM TransactionLineEntity tl WHERE tl.organisationId = :organisationId AND tl.ledgerDispatchStatus IN :dispatchStatuses AND tl.validationStatus IN :validationStatuses AND tl.ledgerDispatchApproved = true")
    List<TransactionLineEntity> findLedgerDispatchPendingTransactionLines(@Param("organisationId") String organisationId,
                                                                          @Param("dispatchStatuses") List<TransactionLine.LedgerDispatchStatus> dispatchStatuses,
                                                                          @Param("validationStatuses") List<ValidationStatus> validationStatuses);

}

