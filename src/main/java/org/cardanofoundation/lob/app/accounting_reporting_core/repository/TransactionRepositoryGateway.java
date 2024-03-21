package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.TransactionConverter;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionRepositoryGateway {

    private final TransactionRepository transactionRepository;
    private final TransactionConverter transactionConverter;

    @Transactional
    public Either<Problem, Boolean> approveTransaction(String transactionId) {
        log.info("Approving transaction: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{transactionId} not found")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        if (tx.validationStatus() == FAILED) {
            return Either.left(Problem.builder()
                    .withTitle("TX_FAILED")
                    .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        return Either.right(transactionRepository.save(tx.transactionApproved(true)).transactionApproved());
    }

    @Transactional
    // this method should not be used from the frontend, only internal use
    // it does not validate if approval is allowed
    public void approveTransactions(Set<String> transactionIds) {
        log.info("Approving transactions: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .map(tx -> tx.transactionApproved(true))
                .collect(Collectors.toSet());

        transactionRepository.saveAll(transactions);
    }

    @Transactional
    // this method should not be used from the frontend, only internal use
    // it does not validate if approval is allowed
    public void approveTransactionsDispatch(Set<String> transactionIds) {
        log.info("Approving transactions dispatch: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .map(tx -> tx.ledgerDispatchApproved(true))
                .collect(Collectors.toSet());

        transactionRepository.saveAll(transactions);
    }

    @Transactional
    public Either<Problem, Boolean> approveTransactionDispatch(String transactionId) {
        log.info("Approving transaction dispatch: {}", transactionId);

        val txM = transactionRepository.findById(transactionId);

        if (txM.isEmpty()) {
            return Either.left(Problem.builder()
                    .withTitle("TX_NOT_FOUND")
                    .withDetail(STR."Transaction with id \{transactionId} not found")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val tx = txM.orElseThrow();

        if (tx.validationStatus() == FAILED) {
            return Either.left(Problem.builder()
                    .withTitle("TX_FAILED")
                    .withDetail(STR."Cannot approve dispatch for a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        return Either.right(transactionRepository.save(tx.ledgerDispatchApproved(true)).ledgerDispatchApproved());
    }

    public Set<String> readApprovalPendingBlockchainTransactionIds(String organisationId,
                                                                   int limit,
                                                                   boolean transactionApprovalNeeded,
                                                                   boolean ledgerApprovalNeeded
    ) {
        return transactionRepository
                .findTransactionIdsByStatuses(
                        organisationId,
                        List.of(NOT_DISPATCHED),
                        List.of(VALIDATED),
                        transactionApprovalNeeded,
                        ledgerApprovalNeeded,
                        Limit.of(limit));
    }

    public Set<Transaction> findDispatchedTransactions(String organisationId,
                                                       Set<Transaction> transactions) {
        val transactionIds = transactionIds(transactions);

        val seenTransactionStatuses = LedgerDispatchStatus.allDispatchedStatuses();

        val dbTransactions = transactionRepository.findTransactionsByLedgerDispatchStatus(
                organisationId,
                transactionIds,
                seenTransactionStatuses
        );

        return transactionConverter.convertFromDb(dbTransactions);
    }

    public Optional<Transaction> findById(String transactionId) {
        return transactionRepository.findById(transactionId).map(transactionConverter::convert);
    }

    public Set<Transaction> findByAllId(Set<String> transactionIds) {
        val dbTransactions = transactionRepository.findAllById(transactionIds);

        return transactionConverter.convertFromDb(dbTransactions);
    }

    private static Set<String> transactionIds(Set<Transaction> passedTransactions) {
        return passedTransactions
                .stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());
    }

}
