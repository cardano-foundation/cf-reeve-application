package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.IngestionPipelineProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private final IngestionPipelineProcessor ingestionPipelineProcessor;
    private final NotificationViolationHandler notificationViolationHandler;
    private final TransactionConverter transactionConverter;
    private final TransactionRepository transactionRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processIncomingERPEvent(OrganisationTransactions organisationTransactions) {
        val finalTransformationResult = ingestionPipelineProcessor.run(
                organisationTransactions,
                OrganisationTransactions.empty(organisationTransactions.organisationId())
        );

        syncToDb(finalTransformationResult);
        sendNotifications(finalTransformationResult.violations());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private void syncToDb(TransformationResult transformationResult) {
        val passedTransactions = transformationResult.passThroughTransactions();

        val passTxEntities = passedTransactions.transactions().stream()
                .map(transactionConverter::convert)
                .toList();

        transactionRepository.saveAll(passTxEntities);
    }

    private void sendNotifications(Set<Violation> violations) {
        notificationViolationHandler.sendViolationNotifications(violations);
    }

}
