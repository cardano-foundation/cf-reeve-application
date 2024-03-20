package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionBatchAssocEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchAssocRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private final NotificationsSenderService notificationsSenderService;
    private final BusinessRulesPipelineProcessor businessRulesPipelineProcessor;
    private final TransactionBatchService transactionBatchService;
    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;
    private final TransactionBatchAssocRepositoryGateway transactionBatchAssocRepositoryGateway;

    @Transactional
    public void continueIngestion(String organisationId,
                                  int chunkNo,
                                  String batchId,
                                  int totalTransactionsCount,
                                  Set<Transaction> transactions) {

        val finalTransformationResult = businessRulesPipelineProcessor.run(
                new OrganisationTransactions(organisationId, transactions),
                OrganisationTransactions.empty(organisationId),
                new HashSet<>()
        );

        val passedTransactions = finalTransformationResult.passedTransactions().transactions();

        dbUpdateTransactionBatch(batchId, chunkNo, totalTransactionsCount, passedTransactions, transactions);

        // TODO store violations in the database
        notificationsSenderService.sendNotifications(finalTransformationResult.violations());
    }

    @Transactional
    public void initiateIngestion(ERPIngestionStored ingestionStored) {
        log.info("Processing ERPIngestionStored event, event: {}", ingestionStored);

        transactionBatchService.createTransactionBatch(
                ingestionStored.getBatchId(),
                ingestionStored.getInstanceId(),
                ingestionStored.getInitiator(),
                ingestionStored.getFilteringParameters()
        );

        log.info("Finished processing ERPIngestionStored event, event: {}", ingestionStored);
    }

    private void dbUpdateTransactionBatch(String batchId,
                                          int chunkNo,
                                          int totalTransactionsCount,
                                          Set<Transaction> toDispatchTransactions,
                                          Set<Transaction> allTransactions) {
        log.info("Updating transaction batch, batchId: {}, chunkNo:{}, totalTransactionsCount: {}", batchId, chunkNo, totalTransactionsCount);

        transactionRepository.saveAll(transactionConverter.convertToDb(toDispatchTransactions));

        val transactionBatchAssocEntities = allTransactions
                .stream()
                .map(tx -> new TransactionBatchAssocEntity(new TransactionBatchAssocEntity.Id(batchId, tx.getId())))
                .collect(Collectors.toSet());

        transactionBatchAssocRepositoryGateway.storeAll(transactionBatchAssocEntities);
    }

}
