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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.*;
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

    @Transactional
    public void dispatchTransactions() {
        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.id();
            val transactionsToDispatch = transactionEntityRepository.findTransactionsByStatus(organisationId, List.of(STORED, ROLLBACKED));

            log.info("Dispatching transactions for organisation:{}", organisationId);

            createAndSendBlockchainTransactions(organisation.id(), transactionsToDispatch);
        }
    }

    private Optional<BlockchainTransactions> createAndSendBlockchainTransactions(String organisationId,
                                                                                 List<TransactionEntity> transactions) {
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

    private void sendLedgerUpdatedEvents(String organisationId,
                                         List<TransactionEntity> submittedTransactions) {
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
