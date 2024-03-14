package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BatchChunk;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private final NotificationsSenderService notificationsSenderService;
    private final TransactionConverter transactionConverter;
    private final TransactionRepository transactionRepository;
    private final BusinessRulesPipelineProcessor businessRulesPipelineProcessor;

    @Transactional
    public void processIncomingERPEvent(BatchChunk batchChunk) {
        val organisationId = batchChunk.getOrganisationId();

        val finalTransformationResult = businessRulesPipelineProcessor.run(
                new OrganisationTransactions(organisationId, batchChunk.getTransactions()),
                OrganisationTransactions.empty(organisationId),
                new HashSet<>()
        );

        syncToDb(finalTransformationResult, batchChunk);
        notificationsSenderService.sendNotifications(finalTransformationResult.violations());
    }

    @Transactional
    private void syncToDb(TransformationResult transformationResult, BatchChunk batchChunk) {
        val organisationTransactions = transformationResult.organisationTransactions();
        val transactions = organisationTransactions.transactions();

        val passTxEntities = transactions.stream()
                .map(transactionConverter::convert)
                .toList();

        transactionRepository.saveAll(passTxEntities);
    }

}
