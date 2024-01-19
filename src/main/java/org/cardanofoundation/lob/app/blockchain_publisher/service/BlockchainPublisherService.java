package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service("blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void dispatchTransactionsToBlockchains(String organisationId, TransactionLines transactionLines) {
        log.info("dispatchTransactionsToBlockchains...");

        val blockchainDispatchedTxLineIds = transactionLines.entries()
                .stream()
                .map(TransactionLine::id)
                .toList();

        // TODO send data to the blockchain
        // or prepare data to be sent to the blockchain
        // so that user can be prompted to send it

        val statusesMap = blockchainDispatchedTxLineIds.stream()
                .collect(toMap(Function.identity(), v -> TransactionLine.LedgerDispatchStatus.DISPATCHED));

        if (!statusesMap.isEmpty()) {
            log.info("Publishing LedgerChangeEvent command, statusesMap: {}", statusesMap);

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(organisationId, statusesMap));
        }
    }

}
