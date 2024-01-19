package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockchainPublisherEventHandler {

    public final ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void handleLedgerUpdateCommand(LedgerUpdateCommand command) {
        log.info("Received LedgerUpdateCommand command...");

        val blockchainDispatchedTxLineIds = command.txData().transactionLines()
                .stream()
                .map(TransactionLine::id)
                .toList();

        // TODO send data to the blockchain
        // or prepare data to be sent to the blockchain
        // so that user can be prompted to send it

        Map<String, LedgerDispatchStatus> statusesMap = blockchainDispatchedTxLineIds.stream()
                .collect(toMap(Function.identity(), v -> LedgerDispatchStatus.DISPATCHED));

        if (!statusesMap.isEmpty()) {
            log.info("Publishing LedgerChangeEvent command, statusesMap: {}", statusesMap);

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(command.organisationId(), statusesMap));
        }
    }

}
