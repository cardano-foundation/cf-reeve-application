package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionRepositoryReader {

    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Set<Transaction> readBlockchainDispatchPendingTransactions(String organisationId) {
        // TODO what about order by entry date or transaction internal number, etc?

        return transactionRepository
                .findBlockchainPublisherPendingTransactions(
                        organisationId,
                        List.of(NOT_DISPATCHED),
                        List.of(VALIDATED)
                )
                .stream()
                .map(transactionConverter::convert)
                .collect(Collectors.toSet());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Set<Transaction> findDispatchedTransactions(String organisationId,
                                                       Set<Transaction> transactions) {
        // TODO what about order by entry date or transaction internal number, etc?

        val transactionIds = transactionIds(transactions);

        val seenTransactionStatuses = LedgerDispatchStatus.allDispatchedStatuses();

        return transactionRepository.findTransactionsByLedgerDispatchStatus(
                        organisationId,
                        transactionIds,
                        seenTransactionStatuses)
                .stream()
                .map(transactionConverter::convert)
                .collect(Collectors.toSet());
    }

    private static Set<String> transactionIds(Set<Transaction> passedTransactions) {
        return passedTransactions
                .stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());
    }

}
