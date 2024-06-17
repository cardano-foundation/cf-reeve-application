package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionItemRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionVersionAlgo.ERP;

@Service
@Slf4j
@RequiredArgsConstructor
public class DbSynchronisationService {

    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;
    private final TransactionItemRepository transactionItemRepository;
    private final TransactionBatchAssocRepository transactionBatchAssocRepository;
    private final TransactionBatchService transactionBatchService;

    @Transactional
    public void synchronise(String batchId,
                            OrganisationTransactions incomingTransactions,
                            Optional<Integer> totalTransactionsCount,
                            ProcessorFlags flags) {
        val transactions = incomingTransactions.transactions();

        if (transactions.isEmpty()) {
            log.info("No transactions to process, batchId: {}", batchId);
            transactionBatchService.updateTransactionBatchStatusAndStats(batchId, totalTransactionsCount);
            return;
        }

        if (flags.isReprocess()) {
            // TODO should we check if we are NOT changing incomingTransactions which are already marked as dispatched?
            storeTransactions(batchId, incomingTransactions);
        } else {
            val organisationId = incomingTransactions.organisationId();
            processTransactionsForTheFirstTime(batchId, organisationId, transactions, totalTransactionsCount);
        }
    }

    private void processTransactionsForTheFirstTime(String batchId,
                                                    String organisationId,
                                                    Set<TransactionEntity> incomingDetachedTransactions,
                                                    Optional<Integer> totalTransactionsCount) {
        val txIds = incomingDetachedTransactions.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        val databaseTransactionsMap = transactionRepository.findAllById(txIds)
                .stream()
                .collect(Collectors.toMap(TransactionEntity::getId, Function.identity()));

        val toProcessTransactions = new LinkedHashSet<TransactionEntity>();

        for (val incomingTx : incomingDetachedTransactions) {
            val txM = Optional.ofNullable(databaseTransactionsMap.get(incomingTx.getId()));

            val isDispatchMarked = txM.map(TransactionEntity::allApprovalsPassedForTransactionDispatch).orElse(false);
            val notStoredYet = txM.isEmpty();
            val isChanged = notStoredYet || (txM.map(tx -> !areAnyChangesToIncomingTransaction(tx, incomingTx)).orElse(false));

            if (isDispatchMarked && isChanged) {
                log.warn("Transaction cannot be altered, it is already marked as dispatched, transactionNumber: {}", incomingTx.getTransactionInternalNumber());
            }

            if (isChanged && !isDispatchMarked) {
                if (txM.isPresent()) {
                    val attached = txM.orElseThrow();

                    transactionConverter.copyFields(attached, incomingTx);

                    toProcessTransactions.add(attached);
                } else {
                    toProcessTransactions.add(incomingTx);
                }
            }
        }

        storeTransactions(batchId, new OrganisationTransactions(organisationId, toProcessTransactions));

        transactionBatchService.updateTransactionBatchStatusAndStats(batchId, totalTransactionsCount);
    }

    private void storeTransactions(String batchId,
                                   OrganisationTransactions transactions) {
        log.info("Updating transaction batch, batchId: {}", batchId);

        val txs = transactions.transactions();

        for (val tx : txs) {
            val saved = transactionRepository.save(tx);
            saved.getItems().forEach(i -> i.setTransaction(saved));
            transactionItemRepository.saveAll(tx.getItems());
        }

        val transactionBatchAssocEntities = txs
                .stream()
                .map(tx -> {
                    val id = new TransactionBatchAssocEntity.Id(batchId, tx.getId());

                    return transactionBatchAssocRepository.findById(id).orElseGet(() -> new TransactionBatchAssocEntity(id));
                })
                .collect(Collectors.toSet());

        transactionBatchAssocRepository.saveAll(transactionBatchAssocEntities);
    }

    private boolean areAnyChangesToIncomingTransaction(TransactionEntity existingTx,
                                                       TransactionEntity incomingTx) {
        val existingTxVersion = TransactionVersionCalculator.compute(ERP, existingTx);
        val incomingTxVersion = TransactionVersionCalculator.compute(ERP, incomingTx);

        log.info("Existing transaction version: {}, incomingTx:{}", existingTxVersion, incomingTxVersion);

        return !existingTxVersion.equals(incomingTxVersion);
    }

}
