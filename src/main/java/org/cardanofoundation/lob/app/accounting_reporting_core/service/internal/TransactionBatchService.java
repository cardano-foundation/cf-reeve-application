package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionBatchService {

    private final TransactionBatchRepository transactionBatchRepository;
    private final TransactionConverter transactionConverter;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createTransactionBatch(String batchId,
                                       String instanceId,
                                       String initiator,
                                       FilteringParameters filteringParameters) {
        log.info("Creating transaction batch, batchId: {}, initiator: {}, instanceId: {}, filteringParameters: {}", batchId, initiator, instanceId, filteringParameters);

        val transactionBatchEntity = new TransactionBatchEntity()
                .id(batchId)
                .transactions(Set.of()) // initially empty
                .filteringParameters(transactionConverter.convert(filteringParameters))
                .updatedBy(initiator)
                .createdBy(initiator);

        transactionBatchRepository.save((TransactionBatchEntity) transactionBatchEntity);

        log.info("Transaction batch created, batchId: {}", batchId);

        applicationEventPublisher.publishEvent(TransactionBatchCreatedEvent.builder()
                .batchId(batchId)
                .instanceId(instanceId)
                .filteringParameters(filteringParameters)
                .build());
    }

}
