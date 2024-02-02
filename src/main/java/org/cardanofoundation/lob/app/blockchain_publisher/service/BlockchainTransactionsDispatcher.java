package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.BlockchainPublisherRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.STORED;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainTransactionsDispatcher {

    private final BlockchainPublisherRepository blockchainPublisherRepository;

    private final OrganisationPublicApi organisationPublicApi;

    private final L1TransactionCreator l1TransactionCreator;

    private final TransactionSubmissionService transactionSubmissionService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void dispatchTransactions() {
        for (val org : organisationPublicApi.listAll()) {
            val blockPublisher = List.of(STORED);
            val organisationId = org.id();

            val transactionsToDispatch = blockchainPublisherRepository.findTransactionsToDispatch(organisationId, blockPublisher);
            // create a map from transaction id to transaction line
            final Map<String, TransactionLineEntity> transactionLineMap = transactionsToDispatch.stream()
                    .collect(Collectors.toMap(TransactionLineEntity::getId, tx -> tx));

            val serialisedTxE = l1TransactionCreator.createTransactions(transactionsToDispatch);

            if (serialisedTxE.isEmpty()) {
                log.error("Unable to create transactions, {}", serialisedTxE.getLeft());
                continue;
            }

            val serialisedTxs = serialisedTxE.get();
            for (val tx : serialisedTxs) {
                val txId = transactionSubmissionService.submitTransaction(tx.getCompanion());

                log.info("Transaction submitted, txId:{}", txId);
            }
        }
    }

}
