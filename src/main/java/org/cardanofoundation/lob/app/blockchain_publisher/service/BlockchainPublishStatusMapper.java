package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.OnChainAssuranceLevel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class BlockchainPublishStatusMapper {

    public LedgerDispatchStatus convert(BlockchainPublishStatus blockchainPublishStatus,
                                        Optional<OnChainAssuranceLevel> assuranceLevelM) {
        return switch (blockchainPublishStatus) {
            case STORED, ROLLBACKED -> LedgerDispatchStatus.STORED;
            case VISIBLE_ON_CHAIN, SUBMITTED -> LedgerDispatchStatus.DISPATCHED;
            case COMPLETED -> assuranceLevelM.map(level -> {
                if (level == OnChainAssuranceLevel.HIGH) {
                    return LedgerDispatchStatus.COMPLETED;
                }

                return LedgerDispatchStatus.DISPATCHED;
            }).orElse(LedgerDispatchStatus.DISPATCHED);
            case FINALIZED -> LedgerDispatchStatus.FINALIZED;
        };
    }

}
