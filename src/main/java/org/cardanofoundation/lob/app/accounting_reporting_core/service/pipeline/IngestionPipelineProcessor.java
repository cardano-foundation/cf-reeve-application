package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionPipelineProcessor {

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    private final OrganisationPublicApi organisationPublicApi;

    private final Validator validator;

    private final List<PipelineTask> pipelineTasks = new ArrayList<>();

    @PostConstruct
    public void init() {
        pipelineTasks.add(new PreProcessingPipelineTask(transactionRepositoryGateway));

        pipelineTasks.add(new PreCleansingPipelineTask());
        pipelineTasks.add(new PreValidationPipelineTask());

        pipelineTasks.add(new ConversionsPipelineTask(organisationPublicApi));

        pipelineTasks.add(new PostCleansingPipelineTask());
        pipelineTasks.add(new PostValidationPipelineTask(validator));
    }

    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions) {
        val allViolations = new HashSet<Violation>();

        for (val pipelineTask : pipelineTasks) {
            //log.info("Running pipelineTask: {}", pipelineTask.getClass().getSimpleName());

            val transformationResult = pipelineTask.run(
                    passedTransactions,
                    ignoredTransactions
            );

            // TODO refactor this - we do not want to over-write passed in params (anti-pattern)
            passedTransactions = transformationResult.passThroughTransactions();
            ignoredTransactions = transformationResult.ignoredTransactions();
            //log.info("post-violationsCount: {}", transformationResult.violations().size());

            transformationResult.violations().forEach(violation -> {
                if (violation.type() == Violation.Type.FATAL) {
                    log.warn("Violation: {}", violation);
                }
            });

            allViolations.addAll(transformationResult.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), validateNotFailedTransactions(passedTransactions)),
                ignoredTransactions,
                allViolations
        );
    }

    private static Set<Transaction> validateNotFailedTransactions(OrganisationTransactions passedTransactions) {
        return passedTransactions
                .transactions()
                .stream()
                .map(tx -> {
                    if (tx.getValidationStatus() == FAILED) {
                        return tx;
                    }

                    return tx.toBuilder()
                            .validationStatus(VALIDATED)
                            .build();
                })
                .collect(Collectors.toSet());
    }

}
