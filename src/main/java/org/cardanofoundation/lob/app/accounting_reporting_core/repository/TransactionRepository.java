package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

//    @Query(value = "SELECT t FROM accounting_reporting_core.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.id in :transactionIds AND t.validationStatus IN :validationStatuses")
//    Set<TransactionEntity> findByValidationStatus(@Param("organisationId") String organisationId,
//                                                                  @Param("transactionIds") Set<String> transactionIds,
//                                                                  @Param("validationStatuses") Set<ValidationStatus> validationStatuses);

    @Query("SELECT t FROM accounting_reporting_core.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.id in :transactionIds AND t.ledgerDispatchStatus IN :ledgerDispatchStatus ORDER BY t.createdAt ASC")
    Set<TransactionEntity> findTransactionsByLedgerDispatchStatus(@Param("organisationId") String organisationId,
                                                                  @Param("transactionIds") Set<String> transactionIds,
                                                                  @Param("ledgerDispatchStatus") Set<LedgerDispatchStatus> ledgerDispatchStatuses);


//    @Query("SELECT t FROM accounting_reporting_core.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.ledgerDispatchStatus IN :dispatchStatuses AND t.validationStatus IN :validationStatuses AND t.ledgerDispatchApproved = :ledgerDispatchApproved")
//    Set<TransactionEntity> findBlockchainPublisherPendingTransactions(@Param("organisationId") String organisationId,
//                                                                             @Param("dispatchStatuses") List<LedgerDispatchStatus> dispatchStatuses,
//                                                                             @Param("validationStatuses") List<ValidationStatus> validationStatuses,
//                                                                             @Param("ledgerDispatchApproved") boolean ledgerDispatchApproved);

    @Query("SELECT t.id FROM accounting_reporting_core.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.ledgerDispatchStatus IN :dispatchStatuses AND t.validationStatus IN :validationStatuses" +
            " AND (t.ledgerDispatchApproved = :ledgerDispatchApproved" +
            " OR t.transactionApproved = :transactionApproved) " +
            " ORDER BY t.createdAt ASC")
    Set<String> findTransactionIdsByStatuses(@Param("organisationId") String organisationId,
                                             @Param("dispatchStatuses") List<LedgerDispatchStatus> dispatchStatuses,
                                             @Param("validationStatuses") List<ValidationStatus> validationStatuses,
                                             @Param("transactionApproved") boolean transactionApproved,
                                             @Param("ledgerDispatchApproved") boolean ledgerDispatchApproved,
                                             Limit limit);

}

