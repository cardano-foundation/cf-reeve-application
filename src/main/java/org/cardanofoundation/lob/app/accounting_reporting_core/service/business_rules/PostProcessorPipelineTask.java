package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.TransactionRepositoryGateway;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_ALREADY_DISPATCHED;

@RequiredArgsConstructor
@Slf4j
public class PostProcessorPipelineTask implements PipelineTask {

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val newViolations = new HashSet<Violation>();

        val organisationId = passedTransactions.organisationId();
        val transactions = passedTransactions.transactions();

        val allTransactionIds = transactions.stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());

        val dispatchedTransactions = transactionRepositoryGateway.findDispatchedTransactions(organisationId, transactions);

        for (val dispatchedTransaction : dispatchedTransactions) {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.WARN,
                    organisationId,
                    dispatchedTransaction.getId(),
                    TX_ALREADY_DISPATCHED,
                    Map.of()
            );

            newViolations.add(v);
        }
        val dispatchedTransactionIds = dispatchedTransactions.stream().map(Transaction::getId).collect(Collectors.toSet());
        val notDispatchedTransactionIds = Sets.difference(allTransactionIds, dispatchedTransactionIds);

        val toDispatch = Sets.difference(notDispatchedTransactionIds, dispatchedTransactionIds);

//        log.info("Dispatched transactionsCount: {}", dispatchedTransactionIds.size());
//        log.info("Not dispatched transactionsCount: {}", notDispatchedTransactionIds.size());
//        log.info("notDispatchedAndFailedTransactionIds transactionsCount: {}", notDispatchedAndFailedTransactionIds.size());
//        log.info("toDispatch transactionsCount: {}", toDispatch.size());

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
