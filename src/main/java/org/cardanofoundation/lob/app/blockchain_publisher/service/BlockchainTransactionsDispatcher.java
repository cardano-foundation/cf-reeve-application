package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainTransactionWithLines;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.L1SubmissionData;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.BlockchainPublisherRepository;
import org.cardanofoundation.lob.app.blockchain_publisher.service.transation_submit.TransactionSubmissionService;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toMap;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.*;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel.VERY_LOW;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainTransactionsDispatcher {

    private final BlockchainPublisherRepository blockchainPublisherRepository;

    private final OrganisationPublicApi organisationPublicApi;

    private final L1TransactionCreator l1TransactionCreator;

    private final TransactionSubmissionService transactionSubmissionService;

    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void dispatchTransactions() {
        for (val organisation : organisationPublicApi.listAll()) {
            val organisationId = organisation.id();
            val transactionsToDispatch = blockchainPublisherRepository
                    .findTransactionsToDispatch(organisationId, List.of(STORED, ROLLBACKED));

            log.info("Dispatching transactions for organisation:{}", organisationId);

            processTransactions(organisationId, transactionsToDispatch);
        }
    }

    private Optional<BlockchainTransactionWithLines> processTransactions(String organisationId,
                                                                         List<TransactionLineEntity> remainingLines) {
        log.info("Processing transactions for organisation:{}, remaining size:{}", organisationId, remainingLines.size());

        if (remainingLines.isEmpty()) {
            log.info("No more transactions to dispatch for organisation:{}", organisationId);

            return Optional.empty();
        }

        var serialisedTxE = l1TransactionCreator.pullBlockchainTransaction(organisationId, remainingLines);

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
            log.error("Error sending transaction on chain and updating db", e);
        }

        return processTransactions(organisationId, serialisedTx.remainingTransactionLines());
    }

    @Transactional(isolation = SERIALIZABLE)
    private void sendTransactionOnChainAndUpdateDb(BlockchainTransactionWithLines blockchainTransactionWithLines) throws InterruptedException, TimeoutException, ApiException {
        val l1SubmissionData = transactionSubmissionService.submitTransactionWithConfirmation(blockchainTransactionWithLines.serialisedTxData());

        updateTransactionStatuses(l1SubmissionData, blockchainTransactionWithLines);
        sendLedgerUpdatedEvents(blockchainTransactionWithLines);

        log.info("Blockchain transaction submitted, l1SubmissionData:{}", l1SubmissionData);
    }

    private void updateTransactionStatuses(L1SubmissionData l1SubmissionData, BlockchainTransactionWithLines blockchainTransactionWithLines) {
        blockchainTransactionWithLines.submittedTransactionLines().forEach(txLineEntity -> {
            txLineEntity.setL1TransactionHash(l1SubmissionData.txHash());
            txLineEntity.setL1AssuranceLevel(VERY_LOW);
            txLineEntity.setPublishStatus(VISIBLE_ON_CHAIN);
            txLineEntity.setL1AbsoluteSlot(l1SubmissionData.absoluteSlot());

            blockchainPublisherRepository.save(txLineEntity);
        });
    }

    private void sendLedgerUpdatedEvents(BlockchainTransactionWithLines blockchainTransactionWithLines) {
        val organisationId = blockchainTransactionWithLines.organisationId();

        val txStatuses = blockchainTransactionWithLines.submittedTransactionLines()
                .stream()
                .collect(toMap(TransactionLineEntity::getId, value -> {
                    val publishStatus = value.getPublishStatus();
                    val onChainAssuranceLevelM = value.getOnChainAssuranceLevel();

                    return blockchainPublishStatusMapper.convert(publishStatus, onChainAssuranceLevelM);
                }));

        log.info("Sending ledger updated event for organisation:{}, statuses:{}", organisationId, txStatuses);

        applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, txStatuses));
    }

}
