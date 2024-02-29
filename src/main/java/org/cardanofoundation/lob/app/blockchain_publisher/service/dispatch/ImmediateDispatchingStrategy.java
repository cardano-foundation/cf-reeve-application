package org.cardanofoundation.lob.app.blockchain_publisher.service.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class ImmediateDispatchingStrategy implements DispatchingStrategy {

    @Override
    public Set<TransactionEntity> apply(String organisationId,
                                        Set<TransactionEntity> transactions) {
        return transactions;
    }

}
