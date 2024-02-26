package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final TransactionRepository transactionRepository;

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final PIIDataFilteringService piiDataFilteringService;


    @Transactional
    public void updateTransactionsWithNewLedgerDispatchStatuses(Set<LedgerUpdatedEvent.TxStatusUpdate> txStatusUpdates) {
        log.info("Updating dispatch status for statusMapCount: {}", txStatusUpdates.size());

        for (val txStatusUpdate : txStatusUpdates) {
            val txId = txStatusUpdate.getTxId();
            val transactionM = transactionRepository.findById(txId);

            if (transactionM.isEmpty()) {
                log.warn("Transaction not found for id: {}", txId);
                continue;
            }

            val transaction = transactionM.orElseThrow();
            transaction.setLedgerDispatchStatus(txStatusUpdate.getStatus());
            transactionRepository.save(transaction);
        }

        log.info("Updated dispatch status for statusMapCount: {} completed.", txStatusUpdates.size());
    }

//    @Transactional
//    public void dispatchTransactionsToBlockchainPublisher() {
//        log.info("dispatchTransactionsToBlockchainPublisher...");
//
//        for (val organisation : organisationPublicApi.listAll()) {
//            val organisationId = organisation.id();
//
//            val dispatchPendingTransactions = transactionRepositoryGateway.readBlockchainDispatchPendingTransactions(organisationId, dispatchBatchSize);
//            log.info("Processing organisationId: {} - dispatchPendingTransactionsSize: {}", organisationId, dispatchPendingTransactions.size());
//
//            val piiFilteredOutTransactions = piiDataFilteringService.apply(dispatchPendingTransactions);
//
//            applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(new OrganisationTransactions(organisationId, piiFilteredOutTransactions)));
//
//            log.info("Publishing PublishToTheLedgerEvent...");
//        }
//    }

    @Transactional
    public void dispatchTransactionToBlockchainPublisher(String organisationId, Set<String> transactionIds) {
        log.info("dispatchTransactionToBlockchainPublisher, txIds: {}", transactionIds);

        val dispatchPendingTransactions = transactionRepositoryGateway.findByAllId(transactionIds);

        if (dispatchPendingTransactions.isEmpty()) {
            log.warn("Transaction not found for id: {}", transactionIds);
            return;
        }

        val piiFilteredOutTransactions = piiDataFilteringService.apply(dispatchPendingTransactions);

        applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(new OrganisationTransactions(organisationId, piiFilteredOutTransactions)));
    }

}
