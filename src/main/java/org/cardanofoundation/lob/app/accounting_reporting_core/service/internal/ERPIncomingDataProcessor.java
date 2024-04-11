package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private final BusinessRulesPipelineProcessor businessRulesPipelineProcessor;
    private final TransactionBatchService transactionBatchService;
    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;
    private final TransactionBatchAssocRepository transactionBatchAssocRepository;

    @Transactional
    public void continueIngestion(String organisationId,
                                  String batchId,
                                  Optional<Integer> totalTransactionsCount,
                                  Set<Transaction> transactions,
                                  boolean reprocess) {
        log.info("Processing ERPTransactionChunk event, batchId: {}, transactions: {}", batchId, transactions.size());

        val finalTransformationResult = businessRulesPipelineProcessor.run(
                new OrganisationTransactions(organisationId, transactions),
                OrganisationTransactions.empty(organisationId),
                reprocess ? ProcessorFlags.builder().skipDeduplicationCheck(true).build() : ProcessorFlags.defaultFlags()
        );

        val passedTransactions = finalTransformationResult.passedTransactions().transactions();
        log.info("PASSING transactions: {}", passedTransactions.size());

        dbUpdateTransactionBatch(batchId, passedTransactions);

        transactionBatchService.updateTransactionBatchStatusAndStats(batchId, totalTransactionsCount);
    }

    @Transactional
    public void initiateIngestion(ERPIngestionStored ingestionStored) {
        log.info("Processing ERPIngestionStored event, event: {}", ingestionStored);

        transactionBatchService.createTransactionBatch(
                ingestionStored.getBatchId(),
                ingestionStored.getInstanceId(),
                ingestionStored.getInitiator(),
                ingestionStored.getUserExtractionParameters(),
                ingestionStored.getSystemExtractionParameters()
        );

        log.info("Finished processing ERPIngestionStored event, event: {}", ingestionStored);
    }

    private void dbUpdateTransactionBatch(String batchId,
                                          Set<Transaction> toDispatchTransactions) {
        log.info("Updating transaction batch, batchId: {}", batchId);

        val dbTransactions= transactionConverter.convertToDb(toDispatchTransactions);

        transactionRepository.saveAll(dbTransactions);

        val transactionBatchAssocEntities = toDispatchTransactions
                .stream()
                .map(tx -> {
                    val id = new TransactionBatchAssocEntity.Id(batchId, tx.getId());

                    return transactionBatchAssocRepository.findById(id).orElseGet(() -> new TransactionBatchAssocEntity(id));
                })
                .collect(Collectors.toSet());

        transactionBatchAssocRepository.saveAll(transactionBatchAssocEntities);
    }

}
