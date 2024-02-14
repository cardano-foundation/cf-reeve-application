package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountingCoreRepository extends JpaRepository<TransactionLineEntity, String> {

    @Query("SELECT tl FROM accounting_reporting_core.TransactionLineEntity tl WHERE tl.organisation.id = :organisationId AND tl.transactionInternalNumber = :txInternalNumber")
    List<TransactionLineEntity> findByInternalTransactionNumber(@Param("organisationId") String organisationId,
                                                                @Param("txInternalNumber") String txInternalNumber);

    @Query("SELECT tl.id FROM accounting_reporting_core.TransactionLineEntity tl WHERE tl.organisation.id = :organisationId AND tl.id in :txLineIds AND tl.ledgerDispatchStatus IN :ledgerDispatchStatus")
    List<String> findTransactionLinesByLedgerDispatchStatus(@Param("organisationId") String organisationId,
                                                            @Param("txLineIds") List<String> txLineIds,
                                                            @Param("ledgerDispatchStatus") List<LedgerDispatchStatus> ledgerDispatchStatuses);

    @Query("SELECT tl FROM accounting_reporting_core.TransactionLineEntity tl WHERE tl.organisation.id = :organisationId AND tl.ledgerDispatchStatus IN :dispatchStatuses AND tl.validationStatus IN :validationStatuses AND tl.ledgerDispatchApproved = true")
    List<TransactionLineEntity> findLedgerDispatchPendingTransactionLines(@Param("organisationId") String organisationId,
                                                                          @Param("dispatchStatuses") List<LedgerDispatchStatus> dispatchStatuses,
                                                                          @Param("validationStatuses") List<ValidationStatus> validationStatuses);

}

