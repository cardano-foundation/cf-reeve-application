package org.cardanofoundation.lob.sourceapi.repository;

import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerEventRepository extends JpaRepository<LedgerEvent, String> {
}
