package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockchainPublisherRepository extends JpaRepository<TransactionLineEntity, String> {

}

