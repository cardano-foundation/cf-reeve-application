package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.TransactionRepositoryReader;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class PreProcessingPipelineTask implements PipelineTask {

    private final TransactionRepositoryReader transactionRepositoryReader;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions) {
        val newViolations = new HashSet<Violation>();

        val organisationId = passedTransactions.organisationId();
        val transactions = passedTransactions.transactions();

        val allTransactionIds = transactions.stream().map(Transaction::getId)
                .collect(Collectors.toSet());

        val dispatchedTransactions = transactionRepositoryReader.findDispatchedTransactions(organisationId, transactions);

        for (val dispatchedTransaction : dispatchedTransactions) {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.WARN,
                    organisationId,
                    dispatchedTransaction.getId(),
                    Violation.Code.TX_ALREADY_DISPATCHED,
                    Map.of()
            );

            newViolations.add(v);
        }
        val dispatchedTransactionIds = dispatchedTransactions.stream().map(Transaction::getId).collect(Collectors.toSet());
        val notDispatchedTransactionIds = Sets.difference(allTransactionIds, dispatchedTransactionIds);

        // if transactions have failed before - we should ignore them and not process them again
        val notDispatchedAndFailedTransactionIds = transactionRepositoryReader.findAllFailedTransactionIds(organisationId, notDispatchedTransactionIds);

        val toDispatch = Sets.difference(notDispatchedTransactionIds, notDispatchedAndFailedTransactionIds);

        log.info("Dispatched transactionsCount: {}", dispatchedTransactionIds.size());
        log.info("Not dispatched transactionsCount: {}", notDispatchedTransactionIds.size());
        log.info("notDispatchedAndFailedTransactionIds transactionsCount: {}", notDispatchedAndFailedTransactionIds.size());
        log.info("toDispatch transactionsCount: {}", toDispatch.size());

        val toDispatchTransactions = transactions.stream()
                .filter(tx -> toDispatch.contains(tx.getId()))
                .collect(Collectors.toSet());

        return new TransformationResult(
                new OrganisationTransactions(organisationId, toDispatchTransactions),
                new OrganisationTransactions(organisationId, dispatchedTransactions),
                newViolations
        );
    }

}
