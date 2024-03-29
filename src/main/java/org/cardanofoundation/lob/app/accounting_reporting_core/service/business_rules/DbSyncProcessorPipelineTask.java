package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_CANNOT_BE_ALTERED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Source.ERP;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.WARN;

@RequiredArgsConstructor
@Slf4j
public class DbSyncProcessorPipelineTask implements PipelineTask {

    private final TransactionRepositoryGateway transactionRepositoryGateway;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val organisationId = passedTransactions.organisationId();
        val incomingTransactions = passedTransactions.transactions();

        if (incomingTransactions.isEmpty()) {
            return new TransformationResult(passedTransactions, ignoredTransactions, allViolationUntilNow);
        }

        val txIds = incomingTransactions.stream()
                .map(Transaction::getId)
                .collect(Collectors.toSet());

        val databaseTransactionsMap = transactionRepositoryGateway.findByAllId(txIds)
                .stream()
                .collect(Collectors.toMap(Transaction::getId, Function.identity()));

        val newViolations = new HashSet<>(allViolationUntilNow);
        val toProcessTransactions = new HashSet<Transaction>();
        val toIgnoreTransactions = new HashSet<Transaction>();

        for (val incomingTx : incomingTransactions) {
            val txM = Optional.ofNullable(databaseTransactionsMap.get(incomingTx.getId()));

            val isDispatchMarked = txM.map(Transaction::allApprovalsPassedForTransactionDispatch).orElse(false);
            val notStoredYet = txM.isEmpty();
            val isChanged = notStoredYet || (txM.map(tx -> !tx.isTheSameBusinessWise(incomingTx)).orElse(false));

            if (isDispatchMarked && isChanged) {
                val violation = Violation.create(
                        WARN,
                        ERP,
                        organisationId,
                        incomingTx.getId(),
                        TX_CANNOT_BE_ALTERED,
                        DbSyncProcessorPipelineTask.class.getName(),
                        Map.of("transactionNumber", incomingTx.getInternalTransactionNumber())
                );

                newViolations.add(violation);
            }

            if (isChanged) {
                toProcessTransactions.add(incomingTx);
            } else {
                toIgnoreTransactions.add(incomingTx);
            }
        }

        return new TransformationResult(
                new OrganisationTransactions(organisationId, toProcessTransactions),
                new OrganisationTransactions(organisationId, toIgnoreTransactions),
                newViolations
        );
    }

}
