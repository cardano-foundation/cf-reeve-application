package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class BlockchainPublishStatusMapper {

    public TransactionLine.LedgerDispatchStatus convert(BlockchainPublishStatus blockchainPublishStatus,
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
