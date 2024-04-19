package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ERPIngestionStored;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.DbSynchronisationService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.ProcessorFlags;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private final BusinessRulesPipelineProcessor businessRulesPipelineProcessor;
    private final TransactionBatchService transactionBatchService;
    private final DbSynchronisationService dbSynchronisationService;

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

    @Transactional
    public void continueIngestion(String organisationId,
                                  String batchId,
                                  Optional<Integer> totalTransactionsCount,
                                  Set<TransactionEntity> transactions,
                                  ProcessorFlags processorFlags) {
        log.info("Processing ERPTransactionChunk event, batchId: {}, transactions: {}", batchId, transactions.size());

        // run or re-run business rules
        val finalTransformationResult = businessRulesPipelineProcessor.run(
                new OrganisationTransactions(organisationId, transactions),
                OrganisationTransactions.empty(organisationId)
        );

        log.info("PASSING transactions: {}", transactions.size());

        dbSynchronisationService.synchroniseAndFlushToDb(batchId,
                finalTransformationResult.passedTransactions(),
                totalTransactionsCount,
                processorFlags
        );
    }

}
