package org.cardanofoundation.lob.app.accounting_reporting_core.service;

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
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionBatchService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void synchroniseAndFlushToDb(String batchId,
                                        OrganisationTransactions transactions,
                                        Optional<Integer> totalTransactionsCount,
                                        ProcessorFlags flags) {
        if (transactions.transactions().isEmpty()) {
            return;
        }
         if (flags.isReprocess()) {
            // TODO should we check if we are NOT changing transactions which are already marked as dispatched?
            dbUpdateTransactionBatch(batchId, transactions);
            return;
        }

        val organisationId = transactions.organisationId();

        val incomingDetachedTransactions = transactions.transactions();

        val txIds = incomingDetachedTransactions.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        val databaseTransactionsMap = transactionRepository.findAllById(txIds)
                .stream()
                .collect(Collectors.toMap(TransactionEntity::getId, Function.identity()));

        val toProcessTransactions = new HashSet<TransactionEntity>();

        for (val incomingTx : incomingDetachedTransactions) {
            val txM = Optional.ofNullable(databaseTransactionsMap.get(incomingTx.getId()));

            val isDispatchMarked = txM.map(TransactionEntity::allApprovalsPassedForTransactionDispatch).orElse(false);
            val notStoredYet = txM.isEmpty();
            val isChanged = notStoredYet || (txM.map(tx -> !tx.isTheSameBusinessWise(incomingTx)).orElse(false));

            if (isDispatchMarked && isChanged) {
                log.warn("Transaction cannot be altered, it is already marked as dispatched, transactionNumber: {}", incomingTx.getTransactionInternalNumber());
//                val v = Violation.builder()
//                        .code(TX_CANNOT_BE_ALTERED)
//                        .type(WARN)
//                        .source(LOB)
//                        .processorModule(this.getClass().getSimpleName())
//                        .bag(
//                                Map.of(
//                                        "transactionNumber", incomingTx.getTransactionInternalNumber()
//                                )
//                        )
//                        .build();
//
//                incomingTx.addViolation(v);
            }

            if (isChanged && !isDispatchMarked) {
                if (txM.isPresent()) {
                    val attached = txM.orElseThrow();
                    transactionConverter.useFieldsFromDetached(attached, incomingTx);

                    toProcessTransactions.add(attached);
                } else {
                    toProcessTransactions.add(incomingTx);
                }
            }
        }

        dbUpdateTransactionBatch(batchId, new OrganisationTransactions(organisationId, toProcessTransactions));
        transactionBatchService.updateTransactionBatchStatusAndStats(batchId, totalTransactionsCount);
    }

    private void dbUpdateTransactionBatch(String batchId,
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

}
