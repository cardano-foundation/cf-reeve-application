package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.BlockchainPublisherRepository;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.BlockchainTransactionSubmissionService;
import org.cardanofoundation.lob.app.blockchain_publisher.util.WithExtraIds;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainTransactionsDispatcher {

    private final BlockchainPublisherRepository blockchainPublisherRepository;

    private final OrganisationPublicApi organisationPublicApi;

    private final L1TransactionCreator l1TransactionCreator;

    private final BlockchainTransactionSubmissionService transactionSubmissionService;

    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void dispatchTransactions() {
        for (val org : organisationPublicApi.listAll()) {
            val organisationId = org.id();

            val transactionsToDispatch = blockchainPublisherRepository.findTransactionsToDispatch(organisationId, List.of(STORED, ROLLBACKED));

            // create a map from transaction line id to transaction line
            val transactionLineMap = transactionsToDispatch.stream()
                    .collect(toMap(TransactionLineEntity::getId, txLine -> txLine));

            log.info("Dispatching transactions for organisation:{}", organisationId);

            val serialisedTxE = l1TransactionCreator.createTransactions(transactionsToDispatch);

            if (serialisedTxE.isEmpty()) {
                log.warn("No transactions to dispatch for organisation:{}", organisationId);
                continue;
            }

            val serialisedBlockchainTransactionsWithIds = serialisedTxE.get();

            for (val blockchainTransaction : serialisedBlockchainTransactionsWithIds) {
                processTransaction(blockchainTransaction, transactionLineMap, organisationId);
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private void processTransaction(WithExtraIds<byte[]> blockchainTransaction,
                                    Map<String, TransactionLineEntity> transactionLineMap, String organisationId) {
        val txId = transactionSubmissionService.submitTransaction(blockchainTransaction.getCompanion());

        val revelantTransactionLinesMap = transactionLineMap.entrySet()
                .stream()
                .filter(entry -> blockchainTransaction.getIds().contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        updateTransactionStatuses(TransactionUtil.getTxHash(blockchainTransaction.getCompanion()), revelantTransactionLinesMap);
        sendLedgerUpdatedEvents(organisationId, revelantTransactionLinesMap);



        log.info("Blockchain transaction submitted, txId:{}", txId);
    }

    private void updateTransactionStatuses(String l1TransactionHash, Map<String, TransactionLineEntity> transactionLineMap) {
        transactionLineMap.forEach((txLineId, txLineEntity) -> {
            txLineEntity.setL1TransactionHash(l1TransactionHash);
            txLineEntity.setPublishStatus(SUBMITTED);
            blockchainPublisherRepository.save(txLineEntity);
        });
    }

    private void sendLedgerUpdatedEvents(String organisationId,
                                         Map<String, TransactionLineEntity> transactionLineMap) {
        val txStatuses = transactionLineMap.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> {
                    val publishStatus = entry.getValue().getPublishStatus();
                    val onChainAssuranceLevelM = entry.getValue().getOnChainAssuranceLevel();

                    return blockchainPublishStatusMapper.convert(publishStatus, onChainAssuranceLevelM);
                }));

        applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, txStatuses));
    }

}
