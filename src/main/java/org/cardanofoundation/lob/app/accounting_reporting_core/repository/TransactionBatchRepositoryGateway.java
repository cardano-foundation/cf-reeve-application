package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionBatchRepositoryGateway {

    private final TransactionBatchRepository transactionBatchRepository;

    @Transactional
    public Optional<TransactionBatchEntity> findById(String batchId) {
        return transactionBatchRepository.findById(batchId);
    }

}
