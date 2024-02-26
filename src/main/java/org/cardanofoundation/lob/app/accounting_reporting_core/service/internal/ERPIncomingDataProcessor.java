package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.DefaultBusinessRulesPipelineProcessor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PostProcessorPipelineTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ERPIncomingDataProcessor {

    private BusinessRulesPipelineProcessor businessRulesPipelineProcessor;
    private final NotificationViolationHandler notificationViolationHandler;
    private final TransactionConverter transactionConverter;
    private final TransactionRepository transactionRepository;
    private final TransactionRepositoryGateway transactionRepositoryGateway;

    @PostConstruct
    public void init() {
         businessRulesPipelineProcessor = new DefaultBusinessRulesPipelineProcessor(List.of(new PostProcessorPipelineTask(transactionRepositoryGateway)));
    }

    @Transactional
    public void processIncomingERPEvent(OrganisationTransactions organisationTransactions) {
        val finalTransformationResult = businessRulesPipelineProcessor.run(
                organisationTransactions,
                OrganisationTransactions.empty(organisationTransactions.organisationId())
        );

        syncToDb(finalTransformationResult);
        sendNotifications(finalTransformationResult.violations());
    }

    @Transactional
    private void syncToDb(TransformationResult transformationResult) {
        val organisationTransactions = transformationResult.organisationTransactions();
        val transactions = organisationTransactions.transactions();

        val passTxEntities = transactions.stream()
                .map(transactionConverter::convert)
                .toList();

        transactionRepository.saveAll(passTxEntities);
    }

    private void sendNotifications(Set<Violation> violations) {
        notificationViolationHandler.sendViolationNotifications(violations);
    }

}
