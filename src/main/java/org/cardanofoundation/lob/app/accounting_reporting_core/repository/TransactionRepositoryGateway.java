package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionConverter;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionRepositoryGateway {

    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;

    @Transactional
    public void approveTransactions(Set<String> transactionIds) {
        log.info("Approving transactions: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .map(tx -> {
                    tx.setLedgerDispatchApproved(true);

                    return tx;
                })
                .collect(Collectors.toSet());

        transactionRepository.saveAll(transactions);
    }

//    @Transactional
//    public Set<Transaction> readBlockchainDispatchPendingTransactions(String organisationId, int limit) {
//        // TODO what about order by entry date or transaction internal number, etc?
//
//        return transactionRepository
//                .findBlockchainPublisherPendingTransactions(
//                        organisationId,
//                        List.of(NOT_DISPATCHED),
//                        List.of(VALIDATED),
//                        true)
//                .stream()
//                .limit(limit)
//                .map(transactionConverter::convert)
//                .collect(Collectors.toSet());
//    }

    @Transactional
    public Set<String> readApprovalPendingBlockchainTransactionIds(String organisationId, int limit) {
        // TODO what about order by entry date or transaction internal number, etc?

        return transactionRepository
                .findTransactionIdsByStatuses(
                        organisationId,
                        List.of(NOT_DISPATCHED),
                        List.of(VALIDATED),
                        false,
                        Limit.of(limit))
                .stream()
                .collect(Collectors.toSet());
    }

    @Transactional
    public Set<Transaction> findDispatchedTransactions(String organisationId,
                                                       Set<Transaction> transactions) {
        // TODO what about order by entry date or transaction internal number, etc?

        val transactionIds = transactionIds(transactions);

        val seenTransactionStatuses = LedgerDispatchStatus.allDispatchedStatuses();

        return transactionRepository.findTransactionsByLedgerDispatchStatus(
                        organisationId,
                        transactionIds,
                        seenTransactionStatuses
                )
                .stream()
                .map(transactionConverter::convert)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Optional<Transaction> findById(String transactionId) {
        return transactionRepository.findById(transactionId).map(transactionConverter::convert);
    }

    @Transactional
    public Set<Transaction> findByAllId(Set<String> transactionIds) {
        return transactionRepository.findAllById(transactionIds).stream().map(transactionConverter::convert).collect(Collectors.toSet());
    }

//    @Transactional
//    public Set<String> findAllFailedTransactionIds(String organisationId,
//                                                   Set<String> transactionIds) {
//        // TODO what about order by entry date or transaction internal number, etc?
//
//        return transactionRepository.findByValidationStatus(
//                        organisationId,
//                        transactionIds,
//                        Set.of(FAILED))
//                .stream()
//                .map(transactionConverter::convert)
//                .map(Transaction::getId)
//                .collect(Collectors.toSet());
//    }

    private static Set<String> transactionIds(Set<Transaction> passedTransactions) {
        return passedTransactions
                .stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());
    }

}
