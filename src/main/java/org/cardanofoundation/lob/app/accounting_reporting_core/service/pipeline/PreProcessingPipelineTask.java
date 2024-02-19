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
        val allTransactionIds = transactions.stream().map(Transaction::getId).collect(Collectors.toSet());

        val dispatchedTransactions = transactionRepositoryReader.findDispatchedTransactions(organisationId, transactions);

        for (val dispatchedTransaction : dispatchedTransactions) {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.WARN,
                    organisationId,
                    dispatchedTransaction.getId(),
                    "CANNOT_UPDATE_TRANSACTION",
                    Map.of()
            );

            newViolations.add(v);
        }
        val dispatchedTransactionIds = dispatchedTransactions.stream().map(Transaction::getId).collect(Collectors.toSet());
        val notDispatchedTransactionIds = Sets.difference(allTransactionIds, dispatchedTransactionIds);

        val notDispatchedTransactions = transactions.stream()
                .filter(tx -> notDispatchedTransactionIds.contains(tx.getId()))
                .collect(Collectors.toSet());

        return new TransformationResult(
                new OrganisationTransactions(organisationId, notDispatchedTransactions),
                new OrganisationTransactions(organisationId, dispatchedTransactions),
                newViolations
        );
    }

}
