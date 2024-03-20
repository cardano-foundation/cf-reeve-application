package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryGateway;
import org.cardanofoundation.lob.app.support.collections.Partitions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service("blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionConverter transactionConverter;
    private final TransactionEntityRepositoryGateway transactionEntityRepositoryGateway;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Value("${lob.blockchain_publisher.send.batch.size:25}")
    private final int dispatchBatchSize = 25;

    @Transactional
    public void storeTransactionForDispatchLater(String organisationId,
                                                 OrganisationTransactions organisationTransactions) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val txEntities = transactionConverter.convertToDb(organisationTransactions.transactions());

        val allNewAndOldTransactionsStored = transactionEntityRepositoryGateway.storeOnlyNewTransactions(txEntities);

        notifyTransactionStored(organisationTransactions, allNewAndOldTransactionsStored);
    }

    @Transactional
    private void notifyTransactionStored(OrganisationTransactions organisationTransactions,
                                         Set<TransactionEntity> allNewAndOldTransactionsStored) {
        Partitions.partition(allNewAndOldTransactionsStored, dispatchBatchSize).forEach(partition -> {

            val txStatusUpdates = partition.asSet().stream()
                    .map(txEntity -> {
                        val assuranceLevelM = txEntity.getL1SubmissionData()
                                .flatMap(L1SubmissionData::getAssuranceLevel);
                        val blockchainPublishStatusM = txEntity.getL1SubmissionData().flatMap(L1SubmissionData::getPublishStatus);

                        return new LedgerUpdatedEvent.TxStatusUpdate(txEntity.getId(),
                                blockchainPublishStatusMapper.convert(blockchainPublishStatusM, assuranceLevelM));
                    })
                    .collect(Collectors.toSet());

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationTransactions.organisationId(), txStatusUpdates));
        });
    }

}
