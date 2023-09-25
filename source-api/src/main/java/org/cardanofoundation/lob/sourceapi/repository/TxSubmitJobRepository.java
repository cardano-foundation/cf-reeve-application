package org.cardanofoundation.lob.sourceapi.repository;

import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.common.model.TxSubmitJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TxSubmitJobRepository extends JpaRepository<TxSubmitJob, Integer> {

    List<TxSubmitJob> findByJobStatus(final TxSubmitJobStatus jobStatus);

}
