package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TxStatusUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.RejectionStatus.NOT_REJECTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionConverter transactionConverter;
    private final PIIDataFilteringService piiDataFilteringService;

    @Transactional
    public void updateTransactionsWithNewLedgerDispatchStatuses(Map<String, TxStatusUpdate> txStatusUpdatesMap) {
        log.info("Updating dispatch status for statusMapCount: {}", txStatusUpdatesMap.size());

        val txIds = txStatusUpdatesMap.keySet();

        val transactionEntities = transactionRepository.findAllById(txIds);

        for (val tx : transactionEntities) {
            val txStatusUpdate = txStatusUpdatesMap.get(tx.getId());
            tx.setLedgerDispatchStatus(txStatusUpdate.getStatus());
        }

        transactionRepository.saveAll(transactionEntities);

        log.info("Updated dispatch status for statusMapCount: {} completed.", txStatusUpdatesMap.size());
    }

    // TODO this could be also run by a job every 5 minutes and reading txs from db
    @Transactional(propagation = REQUIRES_NEW)
    public void checkIfThereAreTransactionsToDispatch(String organisationId,
                                                      Set<TransactionEntity> transactions) {
        val txIds = transactions.stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());

        log.info("dispatchTransactionToBlockchainPublisher, txIds: {}", txIds);

        val dispatchPendingTransactions = transactions.stream()
                .filter(TransactionEntity::allApprovalsPassedForTransactionDispatch)
                .filter(tx -> tx.getRejectionStatus() == NOT_REJECTED)
                .filter(tx -> tx.getLedgerDispatchStatus() == NOT_DISPATCHED)
                .collect(Collectors.toSet());

        if (dispatchPendingTransactions.isEmpty()) {
            log.info("No transactions to dispatch for organisationId: {}", organisationId);
            return;
        }

        val canonicalTxs = transactionConverter.convertFromDb(dispatchPendingTransactions);
        val piiFilteredOutTransactions = piiDataFilteringService.apply(canonicalTxs);

        applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(organisationId, piiFilteredOutTransactions));
    }

}
