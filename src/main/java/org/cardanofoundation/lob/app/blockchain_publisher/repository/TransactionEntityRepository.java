package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;

import java.util.Set;

public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, String> {

    @Query("SELECT t FROM blockchain_publisher.TransactionEntity t WHERE t.organisation.id = :organisationId AND t.publishStatus IN :publishStatuses")
    Streamable<TransactionEntity> findTransactionsByStatus(@Param("organisationId") String organisationId,
                                                           @Param("publishStatuses") Set<BlockchainPublishStatus> publishStatuses);

}
