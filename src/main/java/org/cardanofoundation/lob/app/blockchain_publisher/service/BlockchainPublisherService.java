package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.repository.BlockchainPublisherRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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

        val txLineIds = transactionLines
                .entries()
                .stream()
                .map(TransactionLine::getId)
                .toList();

        val loadedEntitiesTxs = blockchainPublisherRepository.findAllById(txLineIds)
                .stream()
                .toList();

        val loadedEntitiesTxsIds = loadedEntitiesTxs
                .stream()
                .map(TransactionLineEntity::getId)
                .toList();

        val diffTxLineIds = Sets.difference(Set.copyOf(txLineIds), Set.copyOf(loadedEntitiesTxsIds));

        // we want to convert and store only new tx lines
        val transactionLineEntities = transactionLines
                .entries()
                .stream()
                .filter(tl -> diffTxLineIds.contains(tl.getId()))
                .map(tl -> transactionLineConverter.convert(uploadId, tl))
                .toList();

        val stored = blockchainPublisherRepository.saveAll(transactionLineEntities);

        val newStoredIds = stored
                .stream()
                .map(TransactionLineEntity::getId)
                .collect(Collectors.toSet());

        val newStoredStatusesMap = newStoredIds
                .stream()
                .collect(toMap(Function.identity(), v -> TransactionLine.LedgerDispatchStatus.STORED));

        val oldStoredStatusesMap = loadedEntitiesTxs
                .stream()
                .collect(toMap(Function.identity(), v -> convertValidationStatus(v.getPublishStatus(), v.getOnChainAssuranceLevel())))
                .entrySet()
                .stream()
                .collect(toMap(k -> k.getKey().getId(), Map.Entry::getValue));

        val combinedStatusesStream = Stream.concat(oldStoredStatusesMap.entrySet().stream(), newStoredStatusesMap.entrySet().stream());

        val combinedStatusesMap = combinedStatusesStream.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        log.info("combinedStatusesMap: {}", combinedStatusesMap);

        if (!combinedStatusesMap.isEmpty()) {
            log.info("Publishing LedgerChangeEvent command, statusesMapCount: {}", combinedStatusesMap.size());

            applicationEventPublisher.publishEvent(new LedgerUpdatedEvent(transactionLines.organisationId(), combinedStatusesMap));
        }
    }

    private TransactionLine.LedgerDispatchStatus convertValidationStatus(BlockchainPublishStatus blockchainPublishStatus,
                                                                         Optional<OnChainAssuranceLevel> assuranceLevelM) {
        return switch (blockchainPublishStatus) {
            case STORED, ROLLBACKED -> TransactionLine.LedgerDispatchStatus.STORED;
            case VISIBLE_ON_CHAIN, SUBMITTED -> TransactionLine.LedgerDispatchStatus.DISPATCHED;
            case COMPLETED -> assuranceLevelM.map(level -> {
                if (level == OnChainAssuranceLevel.HIGH) {
                    return TransactionLine.LedgerDispatchStatus.COMPLETED;
                }

                return TransactionLine.LedgerDispatchStatus.DISPATCHED;
            }).orElse(TransactionLine.LedgerDispatchStatus.DISPATCHED);
            case FINALIZED -> TransactionLine.LedgerDispatchStatus.FINALIZED;
        };
    }

}
