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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenesisService {

    private final AccountingCoreRepository accountingCoreRepository;

    @Transactional
    // TODO find better business name for this
    public TransformationResult run(TransactionLines incomingPassthoughTransactionLines,
                                    TransactionLines ignoredTransactionLines) {
        val violations = new HashSet<Violation>();

        val organisationId = incomingPassthoughTransactionLines.organisationId();

        val txLines = incomingPassthoughTransactionLines
                .entries()
                .stream()
                .toList();

        val txLineIds =  txLines.stream()
                .map(TransactionLine::getId)
                .toList();

        val dispatchedTxLineIds = accountingCoreRepository.findDoneTxLineIds(organisationId, txLineIds);

        log.info("dispatchedTxLineIdsCount: {}", dispatchedTxLineIds.size());

        // here are conflicting ones, the ones that have already been dispatched
        val dispatchedTxLines = txLines.stream()
                .filter(txLine -> dispatchedTxLineIds.contains(txLine.getId()))
                .toList();

        dispatchedTxLines.forEach(txLine -> {
            val v = Violation.create(
                    txLine.getId(),
                    Violation.Priority.HIGH,
                    txLine.getInternalTransactionNumber(),
                    "CANNOT_UPDATE_TX_LINES_ERROR"
            );

            violations.add(v);
        });

        val notDispatchedTxLines = Sets.difference(Set.copyOf(incomingPassthoughTransactionLines.entries()), Set.copyOf(dispatchedTxLines))
                .stream()
                .toList();

        return new TransformationResult(new TransactionLines(organisationId, notDispatchedTxLines), new TransactionLines(organisationId, dispatchedTxLines), new TransactionLines(organisationId, List.of()), violations);
    }

}
