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
import java.util.stream.Collectors;

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
                //log.warn("Transaction not found for id: {}", txId);
                continue;
            }

            transactionRepository.save(transactionM.orElseThrow().ledgerDispatchStatus(txStatusUpdate.getStatus()));
        }

        log.info("Updated dispatch status for statusMapCount: {} completed.", txStatusUpdates.size());
    }

    @Transactional
    public void tryToDispatchTransactionToBlockchainPublisher(String organisationId,
                                                              Set<String> transactionIds) {
        log.info("dispatchTransactionToBlockchainPublisher, txIds: {}", transactionIds);

        val dispatchPendingTransactions = transactionRepositoryGateway.findByAllId(transactionIds)
                .stream()
                .filter(tx -> tx.isTransactionApproved() && tx.isLedgerDispatchApproved())
                .collect(Collectors.toSet());

        if (dispatchPendingTransactions.isEmpty()) {
            //log.warn("Transaction not found for id: {}", transactionIds);
            return;
        }

        val piiFilteredOutTransactions = piiDataFilteringService.apply(dispatchPendingTransactions);

        applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(new OrganisationTransactions(organisationId, piiFilteredOutTransactions)));
    }

}
