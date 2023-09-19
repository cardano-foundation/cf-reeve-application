package org.cardanofoundation.lob.txsubmitter.repository;

import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEventRegistrationRepository extends JpaRepository<LedgerEventRegistrationJob, String> {
    List<LedgerEventRegistrationJob> findByJobStatus(final LedgerEventRegistrationJobStatus jobStatus);

    List<LedgerEventRegistrationJob> findByRegistrationIdAndJobStatus(String registrationId, final LedgerEventRegistrationJobStatus jobStatus);
}
