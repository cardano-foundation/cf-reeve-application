package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReIngestionIntents;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionBatchRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReIngestionIntents.ReprocessType.ONLY_FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.zalando.problem.Status.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TransactionBatchRepository transactionBatchRepository;

    private final TransactionConverter transactionConverter;

    private final ERPIncomingDataProcessor erpIncomingDataProcessor;

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    @Value("${accounting-core.max-approval-batch-size:25}")
    private int maxApprovalBatchSize = 25;

    @Transactional
    public void scheduleIngestion(UserExtractionParameters userExtractionParameters) {
        log.info("scheduleIngestion, parameters: {}", userExtractionParameters);

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent(userExtractionParameters, "system"));
    }

    @Transactional
    public Either<Problem, Boolean> scheduleReIngestion(String batchId,
                                                        ReIngestionIntents reIngestionIntents) {
        log.info("scheduleReIngestion..., batchId: {}", batchId);

        val txBatchM = transactionBatchRepository.findById(batchId);
        if (txBatchM.isEmpty()) {
            return Either.left(Problem.builder()
                        .withTitle("TX_BATCH_NOT_FOUND")
                        .withDetail(STR."Transaction batch with id: \{batchId} not found")
                        .withStatus(NOT_FOUND)
                    .build());
        }

        val txBatch = txBatchM.get();

        var dbTxs = txBatch.transactions();
        if (reIngestionIntents.getReprocessType() == ONLY_FAILED) {
            dbTxs = txBatch.transactions().stream()
                   .filter(tx -> tx.validationStatus() == FAILED)
                   .collect(Collectors.toSet());
        }

        val txs = transactionConverter.convertFromDb(dbTxs);

        val totalTxs = txs.size();

        erpIncomingDataProcessor.continueIngestion(txBatch.getOrganisationId(), batchId, Optional.of(totalTxs), txs);

        return Either.right(true);
    }

    @Transactional
    public void readBatchAndApproveTransactionsWithDispatch(String organisationId) {
        log.info("readBatchAndApproveTransactionsWithDispatch, organisationId: {}, maxDispatchBatchSize: {}", organisationId, maxApprovalBatchSize);

        val txIds = transactionRepositoryGateway.readApprovalPendingBlockchainTransactionIds(organisationId, maxApprovalBatchSize, true, true);

        if (txIds.isEmpty()) {
            return;
        }

        approveTransactionsDispatch(organisationId, txIds);
    }

    @Transactional
    public Either<Problem, Boolean> approveTransaction(String txId) {
        return transactionRepositoryGateway.approveTransaction(txId);
    }

    @Transactional
    public Either<Problem, Boolean> approveTransactionDispatch(String txId) {
        return transactionRepositoryGateway.approveTransactionDispatch(txId);
    }

    @Transactional
    public Set<String> approveTransactions(String organisationId, Set<String> transactionIds) {
        log.info("approveTransactions, transactionIds: {}", transactionIds);

        return transactionRepositoryGateway.approveTransactions(organisationId, transactionIds);
    }

    @Transactional
    public Set<String> approveTransactionsDispatch(String organisationId, Set<String> transactionIds) {
        log.info("approveTransactionsDispatch, transactionIds: {}", transactionIds);

        return transactionRepositoryGateway.approveTransactionsDispatch(organisationId, transactionIds);
    }

}
