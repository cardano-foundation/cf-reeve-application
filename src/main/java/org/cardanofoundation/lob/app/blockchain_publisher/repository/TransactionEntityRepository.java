package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, TransactionId> {

    @Query("SELECT t FROM blockchain_publisher.TransactionEntity t WHERE t.id.organisationId = :organisationId AND t.publishStatus IN :publishStatuses")
    List<TransactionEntity> findTransactionsByStatus(@Param("organisationId") String organisationId,
                                                     @Param("publishStatuses") List<BlockchainPublishStatus> publishStatuses);

}
