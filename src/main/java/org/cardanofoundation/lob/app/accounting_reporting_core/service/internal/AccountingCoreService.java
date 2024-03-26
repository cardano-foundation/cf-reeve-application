package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TxsApprovedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.TxsDispatchApprovedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    private int maxApprovalBatchSize = 25;

    @Transactional
    public void scheduleIngestion(UserExtractionParameters fp) {
        log.info("scheduleIngestion, parameters: {}", fp);

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent(fp, "system"));
    }

    @Transactional
    public void readBatchAndApproveTransactions(String organisationId) {
        log.info("readBatchAndApproveTransactions, organisationId: {}, maxDispatchBatchSize: {}", organisationId, maxApprovalBatchSize);

        val txIds = transactionRepositoryGateway.readApprovalPendingBlockchainTransactionIds(organisationId, maxApprovalBatchSize, true, false);

        if (txIds.isEmpty()) {
            log.info("No transactions to dispatch for organisation: {}", organisationId);
            return;
        }

        approveTransactions(organisationId, txIds);
    }

    @Transactional
    public void readBatchAndApproveTransactionsWithDispatch(String organisationId) {
        log.info("readBatchAndApproveTransactionsWithDispatch, organisationId: {}, maxDispatchBatchSize: {}", organisationId, maxApprovalBatchSize);

        val txIds = transactionRepositoryGateway.readApprovalPendingBlockchainTransactionIds(organisationId, maxApprovalBatchSize, true, true);

        if (txIds.isEmpty()) {
            log.info("No transactions to dispatch for organisation: {}", organisationId);
            return;
        }

        approveTransactionsDispatch(organisationId, txIds);
    }


    @Transactional
    public void approveTransactions(String organisationId, Set<String> transactionIds) {
        log.info("approveTransactions, transactionIds: {}", transactionIds);

        transactionRepositoryGateway.approveTransactions(transactionIds);

        applicationEventPublisher.publishEvent(new TxsApprovedEvent(organisationId, transactionIds));
    }

    @Transactional
    public void approveTransactionsDispatch(String organisationId, Set<String> transactionIds) {
        log.info("approveTransactionsDispatch, transactionIds: {}", transactionIds);

        transactionRepositoryGateway.approveTransactionsDispatch(transactionIds);

        applicationEventPublisher.publishEvent(new TxsDispatchApprovedEvent(organisationId, transactionIds));
    }

}
