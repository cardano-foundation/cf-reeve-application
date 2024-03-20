package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionBatchAssocRepositoryGateway {

    private final TransactionBatchAssocRepository transactionBatchRepository;

    @Transactional
    public void store(TransactionBatchAssocEntity transactionBatchAssocEntity) {
        transactionBatchRepository.save(transactionBatchAssocEntity);
    }

    @Transactional
    public void storeAll(Set<TransactionBatchAssocEntity> transactionBatchAssocEntity) {
        transactionBatchRepository.saveAll(transactionBatchAssocEntity);
    }

}
