package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    @Query("SELECT t FROM accounting_reporting_core.TransactionEntity t" +
            " WHERE t.organisation.id = :organisationId " +
            " AND t.validationStatus = 'VALIDATED' " +
            " AND t.ledgerDispatchStatus = 'NOT_DISPATCHED'" +
            " AND t.ledgerDispatchApproved" +
            " AND t.transactionApproved" +
            " AND t.rejectionStatus = 'NOT_REJECTED'" +
            " ORDER BY t.createdAt ASC, t.id ASC")
    Set<TransactionEntity> findDispatchTransactions(@Param("organisationId") String organisationId, Limit limit);

}
