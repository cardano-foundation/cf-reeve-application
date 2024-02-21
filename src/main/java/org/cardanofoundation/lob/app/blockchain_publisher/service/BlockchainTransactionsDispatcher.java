package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactions;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepository;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.TransactionSubmissionService;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.VISIBLE_ON_CHAIN;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.toDispatchStatuses;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel.VERY_LOW;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainTransactionsDispatcher {

    private final TransactionEntityRepository transactionEntityRepository;

    private final OrganisationPublicApi organisationPublicApi;

    private final L1TransactionCreator l1TransactionCreator;

    private final TransactionSubmissionService transactionSubmissionService;

    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${lob.blockchain.publisher.pullBatchSize:50}")
    private int pullBatchSize = 50;

    @Value("${lob.blockchain.publisher.minTransactions:30}")
    private int minTxCount = 35;

    @Transactional
    public void dispatchTransactions() {
        log.info("Dispatching transactions to the cardano blockchain...");

        val dispatchStatuses = toDispatchStatuses();

        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.id();

            val transactionsToDispatch = transactionEntityRepository.findTransactionsByStatus(organisationId, dispatchStatuses)
                    .stream().limit(pullBatchSize)
                    .collect(Collectors.toSet());

            // no point to dispatch less than 30 transactions per physical L1 transaction
            if (transactionsToDispatch.size() < minTxCount) {
                log.warn("Not enough transactions to dispatch for organisationId:{}", organisationId);
                return;
            }

            dispatchTransactionsBatch(organisationId, transactionsToDispatch);
        }
    }

    @Transactional
    public void dispatchTransactionsBatch(String organisationId,
                                          Set<TransactionEntity> transactionEntitiesBatch) {
        log.info("Dispatching transactions for organisation: {}", organisationId);

        val blockchainTransactionsM = createAndSendBlockchainTransactions(organisationId, transactionEntitiesBatch);

        if (blockchainTransactionsM.isEmpty()) {
            log.info("No more transactions to dispatch for organisationId: {}", organisationId);
            return;
        }

        val blockchainTransactions = blockchainTransactionsM.orElseThrow();

        val submittedTxCount = blockchainTransactions.submittedTransactions().size();
        val remainingTxCount = blockchainTransactions.remainingTransactions().size();

        log.info("Submitted tx count:{}, remainingTxCount:{}", submittedTxCount, remainingTxCount);
    }

    @Transactional
    private Optional<BlockchainTransactions> createAndSendBlockchainTransactions(String organisationId,
                                                                                 Set<TransactionEntity> transactions) {
        log.info("Processing transactions for organisation:{}, remaining size:{}", organisationId, transactions.size());

        if (transactions.isEmpty()) {
            log.info("No more transactions to dispatch for organisation:{}", organisationId);

            return Optional.empty();
        }

        var serialisedTxE = l1TransactionCreator.pullBlockchainTransaction(organisationId, transactions);

        if (serialisedTxE.isEmpty()) {
            log.warn("Error, there is more transactions to dispatch for organisation:{}", organisationId);

            return Optional.empty();
        }

        val serialisedTxM = serialisedTxE.get();

        if (serialisedTxM.isEmpty()) {
            log.warn("No transactions to dispatch for organisationId:{}", organisationId);

            return Optional.empty();
        }

        val serialisedTx = serialisedTxM.orElseThrow();
        try {
            sendTransactionOnChainAndUpdateDb(serialisedTx);
        } catch (InterruptedException | TimeoutException | ApiException e) {
            log.error("Error sending transaction on chain and / or updating db", e);
        }

        return createAndSendBlockchainTransactions(organisationId, serialisedTx.remainingTransactions());
    }

    @Transactional
    private void sendTransactionOnChainAndUpdateDb(BlockchainTransactions blockchainTransactions) throws InterruptedException, TimeoutException, ApiException {
        val txData = blockchainTransactions.serialisedTxData();
        val l1SubmissionData = transactionSubmissionService.submitTransactionWithConfirmation(txData);

        updateTransactionStatuses(l1SubmissionData, blockchainTransactions);
        sendLedgerUpdatedEvents(blockchainTransactions.organisationId(), blockchainTransactions.submittedTransactions());

        log.info("Blockchain transaction submitted, l1SubmissionData:{}", l1SubmissionData);
    }

    @Transactional
    private void updateTransactionStatuses(L1SubmissionData l1SubmissionData,
                                           BlockchainTransactions blockchainTransactions) {
        for (val txEntity : blockchainTransactions.submittedTransactions()) {
            txEntity.setL1TransactionHash(l1SubmissionData.txHash());
            txEntity.setL1AssuranceLevel(VERY_LOW);
            txEntity.setPublishStatus(VISIBLE_ON_CHAIN);
            txEntity.setL1AbsoluteSlot(l1SubmissionData.absoluteSlot());
            txEntity.setL1CreationSlot(blockchainTransactions.creationSlot());

            transactionEntityRepository.save(txEntity);
        }
    }

    @Transactional
    private void sendLedgerUpdatedEvents(String organisationId,
                                         Set<TransactionEntity> submittedTransactions) {
        log.info("Sending ledger updated event for organisation:{}, submittedTransactions:{}", organisationId, submittedTransactions.size());

        val txStatuses = submittedTransactions.stream()
                .map(txEntity -> {
                    val publishStatus = txEntity.getPublishStatus();
                    val onChainAssuranceLevelM = txEntity.getOnChainAssuranceLevel();

                    val status = blockchainPublishStatusMapper.convert(publishStatus, onChainAssuranceLevelM);

                    return new LedgerUpdatedEvent.TxStatusUpdate(txEntity.getId(), status);
                })
                .collect(Collectors.toSet());

        log.info("Sending ledger updated event for organisation:{}, statuses:{}", organisationId, txStatuses);

        applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, txStatuses));
    }

}
