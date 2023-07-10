package org.cardanofoundation.lob.sourceapi.repository;

import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TxSubmitJobRepository extends JpaRepository<TxSubmitJob, Integer> {
}
