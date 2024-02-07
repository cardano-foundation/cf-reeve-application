package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.*;

@RequiredArgsConstructor
@Slf4j
public class PreProcessingPipelineTask implements PipelineTask {

    private final AccountingCoreRepository accountingCoreRepository;

    @Override
    public TransformationResult run(TransactionLines passedTransactionLines,
                                    TransactionLines ignoredTransactionLines,
                                    Set<Violation> violations) {
        val organisationId = passedTransactionLines.organisationId();

        val txLines = passedTransactionLines
                .entries()
                .stream()
                .toList();

        val txLineIds =  txLines.stream()
                .map(TransactionLine::getId)
                .toList();

        val dispatchedTxLineIds = accountingCoreRepository.findTransactionLinesByLedgerDispatchStatus(organisationId, txLineIds, List.of(STORED, DISPATCHED, COMPLETED, FINALIZED));

        log.info("dispatchedTxLineIdsCount: {}", dispatchedTxLineIds.size());

        // here are conflicting ones, the ones that have already been dispatched
        val dispatchedTxLines = txLines.stream()
                .filter(txLine -> dispatchedTxLineIds.contains(txLine.getId()))
                .toList();

        val newViolations = new HashSet<>(violations);

        dispatchedTxLines.forEach(dispatchedTxLine -> {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.WARN,
                    dispatchedTxLine.getId(),
                    dispatchedTxLine.getInternalTransactionNumber(),
                    "CANNOT_UPDATE_TX_LINES_ERROR"
            );

            newViolations.add(v);
        });

        val notDispatchedTxLines = Sets.difference(Set.copyOf(passedTransactionLines.entries()), Set.copyOf(dispatchedTxLines))
                .stream()
                .toList();

        return new TransformationResult(
                new TransactionLines(organisationId, notDispatchedTxLines),
                new TransactionLines(organisationId, dispatchedTxLines),
                newViolations
        );
    }

}
