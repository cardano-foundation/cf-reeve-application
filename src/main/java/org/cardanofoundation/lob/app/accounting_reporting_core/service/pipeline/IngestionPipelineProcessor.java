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

    private final List<PipelineTask> pipelineTasks = new ArrayList<>();

    @PostConstruct
    public void init() {
        pipelineTasks.add(new PreProcessingService(accountingCoreRepository));
        pipelineTasks.add(new CleansingPipelineTask());
        pipelineTasks.add(new PreValidationBusinessRulesPipelineTask());
        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi));
        pipelineTasks.add(new PostValidationBusinessRulesPipelineTask());
    }

    @Override
    @Transactional
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    TransactionLines filteredTransactionLines,
                                    Set<Violation> violations) {
        val txLines = passedTransactionLines
                .entries()
                .stream()
                .map(txLine -> txLine.toBuilder()
                .validationStatus(VALIDATED).build())
                .toList();

        passedTransactionLines = new TransactionLines(passedTransactionLines.organisationId(), txLines);

        val organisationId = passedTransactionLines.organisationId();

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

        val validatedTxLines = passedTransactionLines
                .entries()
                .stream()
//                .filter(transactionLine -> transactionLine.getValidationStatus() != ValidationStatus.FAILED)
//                .map(transactionLine -> transactionLine.toBuilder().validationStatus(ValidationStatus.VALIDATED)
//                        .build())
                .toList();

        val finalTransformationResult = new TransformationResult(
                new TransactionLines(organisationId, validatedTxLines),
                ignoredTransactionLines,
                filteredTransactionLines,
                violations
        );

        syncToDb(finalTransformationResult);

        notificationGateway.sendViolationNotifications(finalTransformationResult.violations());

        return finalTransformationResult;
    }

    @Transactional
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
