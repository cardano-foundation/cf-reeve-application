package org.cardanofoundation.lob.app.accounting_reporting_core.repository;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.LedgerService;
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
    private final LedgerService ledgerService;

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
                    .withTitle("CANNOT_APPROVE_FAILED_TX")
                    .withDetail(STR."Cannot approve a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val savedTx = transactionRepository.save(tx.transactionApproved(true));
        val organisationId = savedTx.organisation().getId();

        if (savedTx.transactionApproved()) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(organisationId, Set.of(transactionConverter.convert(savedTx)));

            return Either.right(savedTx.transactionApproved());
        }

        return Either.right(false);
    }

    @Transactional
    public Set<String> approveTransactions(String organisationId, Set<String> transactionIds) {
        log.info("Approving transactions: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .filter(tx -> tx.validationStatus() != FAILED)
                .map(tx -> tx.transactionApproved(true))
                .collect(Collectors.toSet());

        val savedTxs = transactionRepository.saveAll(transactions)
                .stream()
                .map(transactionConverter::convert)
                .collect(Collectors.toSet());

        ledgerService.tryToDispatchTransactionToBlockchainPublisher(organisationId, savedTxs);

        return savedTxs.stream().map(Transaction::getId).collect(Collectors.toSet());
    }

    @Transactional
    public Set<String> approveTransactionsDispatch(String organisationId, Set<String> transactionIds) {
        log.info("Approving transactions dispatch: {}", transactionIds);

        val transactions = transactionRepository.findAllById(transactionIds)
                .stream()
                .filter(tx -> tx.validationStatus() != FAILED)
                .map(tx -> tx.ledgerDispatchApproved(true))
                .collect(Collectors.toSet());

        val savedTxs = transactionRepository.saveAll(transactions)
                .stream()
                .map(transactionConverter::convert)
                .collect(Collectors.toSet());

        ledgerService.tryToDispatchTransactionToBlockchainPublisher(organisationId, savedTxs);

        return savedTxs.stream().map(Transaction::getId).collect(Collectors.toSet());
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
                    .withTitle("CANNOT_APPROVE_FAILED_TX")
                    .withDetail(STR."Cannot approve dispatch for a failed transaction, transactionId: \{transactionId}")
                    .with("transactionId", transactionId)
                    .build()
            );
        }

        val savedTx = transactionRepository.save(tx.ledgerDispatchApproved(true));

        if (savedTx.ledgerDispatchApproved()) {
            ledgerService.tryToDispatchTransactionToBlockchainPublisher(savedTx.organisation().getId(), Set.of(transactionConverter.convert(savedTx)));

            return Either.right(savedTx.ledgerDispatchApproved());
        }

        return Either.right(false);
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

//    public Set<Transaction> findDispatchedTransactions(String organisationId,
//                                                       Set<Transaction> transactions) {
//        val transactionIds = transactionIds(transactions);
//
//        val seenTransactionStatuses = LedgerDispatchStatus.allDispatchedStatuses();
//
//        val dbTransactions = transactionRepository.findTransactionsByLedgerDispatchStatus(
//                organisationId,
//                transactionIds,
//                seenTransactionStatuses
//        );
//
//        return transactionConverter.convertFromDb(dbTransactions);
//    }

    public Optional<Transaction> findById(String transactionId) {
        return transactionRepository.findById(transactionId).map(transactionConverter::convert);
    }

    public Set<Transaction> findByAllId(Set<String> transactionIds) {
        val dbTransactions = transactionRepository.findAllById(transactionIds);

        return transactionConverter.convertFromDb(dbTransactions);
    }

}
