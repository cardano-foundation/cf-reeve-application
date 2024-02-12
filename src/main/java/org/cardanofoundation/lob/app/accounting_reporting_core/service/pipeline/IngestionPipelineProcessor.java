package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.NotificationGateway;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.TransactionLineConverter;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionPipelineProcessor implements PipelineTask {

    private final AccountingCoreRepository accountingCoreRepository;

    private final TransactionLineConverter transactionLineConverter;

    private final OrganisationPublicApi organisationPublicApi;

    private final NotificationGateway notificationGateway;

    private final Validator validator;

    private final List<PipelineTask> pipelineTasks = new ArrayList<>();

    @PostConstruct
    public void init() {
        pipelineTasks.add(new PreProcessingPipelineTask(accountingCoreRepository));

        pipelineTasks.add(new PreCleansingPipelineTask());
        pipelineTasks.add(new PreValidationPipelineTask());

        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi));

        pipelineTasks.add(new PostCleansingPipelineTask());
        pipelineTasks.add(new PostValidationPipelineTask(validator));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines
                .entries()
                .stream()
                .map(txLine -> txLine.toBuilder()
                        .validationStatus(VALIDATED)
                        .build())
                .toList();

        passedTransactionLines = new TransactionLines(passedTransactionLines.organisationId(), txLines);

        for (val pipelineTask : pipelineTasks) {
            log.info("Running pipelineTask: {}", pipelineTask.getClass().getSimpleName());

            val transformationResult = pipelineTask.run(
                    passedTransactionLines,
                    ignoredTransactionLines,
                    violations
            );

            passedTransactionLines = transformationResult.passThroughTransactionLines();
            ignoredTransactionLines = transformationResult.ignoredTransactionLines();
            violations = transformationResult.violations();

            log.info("post-violationsCount: {}", violations.size());

            violations.forEach(violation -> {
                if (violation.type() == Violation.Type.FATAL) {
                    log.warn("violation: {}", violation);
                }
            });

        }

        return new TransformationResult(
                passedTransactionLines,
                ignoredTransactionLines,
                violations
        );
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void syncToDb(TransformationResult transformationResult) {
        val passedTxLines = transformationResult.passThroughTransactionLines();

        val passedTxLineEntities = passedTxLines.entries().stream()
                .map(transactionLineConverter::convert)
                .toList();

        accountingCoreRepository.saveAll(passedTxLineEntities);
    }

    public void sendNotifications(Set<Violation> violations) {
        notificationGateway.sendViolationNotifications(violations);
    }

}
