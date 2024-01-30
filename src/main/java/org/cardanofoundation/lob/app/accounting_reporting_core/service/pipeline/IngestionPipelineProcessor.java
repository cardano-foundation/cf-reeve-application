package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
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

// call to start extraction

// call to approve transaction line ids

//

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionPipelineProcessor implements PipelineTask {

    private final AccountingCoreRepository accountingCoreRepository;

    private final TransactionLineConverter transactionLineConverter;

    private final OrganisationPublicApi organisationPublicApi;

    private final NotificationGateway notificationGateway;

    private final List<PipelineTask> pipelineTasks = new ArrayList<>();

    @PostConstruct
    public void init() {
        pipelineTasks.add(new PreProcessingPipelineTask(accountingCoreRepository));
        pipelineTasks.add(new CleansingPipelineTask());
        pipelineTasks.add(new PreValidationPipelineTask());
        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi));
        pipelineTasks.add(new PostValidationPipelineTask());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines
                .entries()
                .stream()
                .map(txLine -> txLine.toBuilder()
                        .validationStatus(VALIDATED)
                        .build())
                .toList();

        passedTransactionLines = new TransactionLines(passedTransactionLines.organisationId(), txLines);

        log.info("pre-passedTransactionLinesCount: {}", passedTransactionLines.entries().size());
        log.info("pre-ignoredTransactionLinesCount: {}", ignoredTransactionLines.entries().size());
        log.info("pre-filteredTransactionLinesCount: {}", filteredTransactionLines.entries().size());
        log.info("pre-violationsCount: {}", violations.size());

        for (val pipelineTask : pipelineTasks) {
            log.info("Running pipelineTask: {}", pipelineTask.getClass().getSimpleName());

            val transformationResult = pipelineTask.run(
                    passedTransactionLines,
                    ignoredTransactionLines,
                    filteredTransactionLines,
                    violations
            );

            passedTransactionLines = transformationResult.passThroughTransactionLines();
            ignoredTransactionLines = transformationResult.ignoredTransactionLines();
            filteredTransactionLines = transformationResult.filteredTransactionLines();
            violations = transformationResult.violations();

            for (val passedTransactionLine : passedTransactionLines.entries()) {
                log.info("passedTransactionLine: {}", passedTransactionLine);
            }

            log.info("post-passedTransactionLinesCount: {}", passedTransactionLines.entries().size());
            log.info("post-ignoredTransactionLinesCount: {}", ignoredTransactionLines.entries().size());
            log.info("post-filteredTransactionLinesCount: {}", filteredTransactionLines.entries().size());
            log.info("post-violationsCount: {}", violations.size());
        }

        val finalTransformationResult = new TransformationResult(
                passedTransactionLines,
                ignoredTransactionLines,
                filteredTransactionLines,
                violations
        );

        syncToDb(finalTransformationResult);

        notificationGateway.sendViolationNotifications(finalTransformationResult.violations());

        return finalTransformationResult;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private void syncToDb(TransformationResult transformationResult) {
        val passedTxLines = transformationResult.passThroughTransactionLines();
        val filteredTxLines = transformationResult.filteredTransactionLines();

        val passedTxLineEntities = passedTxLines.entries().stream()
                .map(transactionLineConverter::convert)
                .toList();

        val filteredIds = filteredTxLines.entries()
                .stream()
                .map(TransactionLine::getId)
                .toList();

        accountingCoreRepository.saveAll(passedTxLineEntities);
        accountingCoreRepository.deleteAllById(filteredIds);
    }

}
