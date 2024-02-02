package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockchainPublisherRepository extends JpaRepository<TransactionLineEntity, String> {


    @Query("SELECT tl FROM blockchain_publisher.TransactionLineEntity tl WHERE tl.organisationId = :organisationId AND tl.publishStatus IN :publishStatuses")
    List<TransactionLineEntity> findTransactionsToDispatch(@Param("organisationId") String organisationId,
                                                           @Param("publishStatuses") List<BlockchainPublishStatus> publishStatuses);

}
