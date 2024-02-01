package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.BlockchainPublisherRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.SAVE_ACK;

@Service("blockchainPublisherService")
@RequiredArgsConstructor
@Slf4j
public class BlockchainPublisherService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionLineConverter transactionLineConverter;
    private final BlockchainPublisherRepository blockchainPublisherRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void dispatchTransactionsToBlockchains(UUID uploadId,
                                                  TransactionLines transactionLines) {
        log.info("dispatchTransactionsToBlockchains..., uploadId:{}", uploadId);

        val transactionLineEntities = transactionLines
                .entries()
                .stream()
                .map(tl -> transactionLineConverter.convert(uploadId, tl))
                .toList();

        val stored = blockchainPublisherRepository.saveAll(transactionLineEntities);

        val storedIds = stored
                .stream()
                .map(TransactionLineEntity::getId)
                .collect(Collectors.toSet());

        val statusesMap = storedIds
                .stream()
                .collect(toMap(Function.identity(), v -> SAVE_ACK));

        if (!statusesMap.isEmpty()) {
            log.info("Publishing LedgerChangeEvent command, statusesMapCount: {}", statusesMap.size());

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(transactionLines.organisationId(), statusesMap));
        }
    }

}
