package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.TransactionEntityRepositoryReader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service("blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionLineConverter transactionLineConverter;
    private final TransactionEntityRepositoryReader transactionEntityRepositoryReader;
    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void dispatchTransactionsToBlockchains(String organisationId,
                                                  TransactionLines transactionLines) {
        log.info("dispatchTransactionsToBlockchains..., orgId:{}", organisationId);

        val transactions = transactionLines.toTransactions();
        val txEntities = transactions
                .stream()
                .filter(tx -> !tx.getTransactionLines().isEmpty())
                .map(tx -> transactionLineConverter.convert(organisationId, tx))
                .toList();

        val allTxEntitiesMerged = transactionEntityRepositoryReader.storeOnlyNewTransactions(txEntities);

        val txStatusUpdatesList = allTxEntitiesMerged
                .stream()
                .map(txEntity -> {
                    val status = blockchainPublishStatusMapper.convert(txEntity.getPublishStatus(), txEntity.getOnChainAssuranceLevel());

                    return new LedgerUpdatedEvent.TxStatusUpdate(txEntity.getId().getTransactionInternalNumber(), status);
                })
                .collect(Collectors.toSet());

        if (!txStatusUpdatesList.isEmpty()) {
            log.info("Publishing LedgerChangeEvent command, statusesMap: {}", txStatusUpdatesList);

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(transactionLines.organisationId(), txStatusUpdatesList));
        }
    }

}
