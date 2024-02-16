package org.cardanofoundation.lob.app.blockchain_publisher.repository;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEntityRepositoryReader {

    private final TransactionEntityRepository transactionEntityRepository;

    @Transactional
    public Set<TransactionEntity> storeOnlyNewTransactions(Set<TransactionEntity> transactionEntities) {
        log.info("StoreOnlyNewTransactions..., transactionEntitiesCount:{}", transactionEntities.size());

        // check in db if transaction already exists
        // store only the ones that were new
        // return status for the ones that were not new

        val allTransactions = transactionEntities.stream().collect(toSet());

        val txIds = transactionEntities.stream()
                .map(TransactionEntity::getId)
                .collect(toSet());

        val existingTransactions = transactionEntityRepository
                .findAllById(txIds)
                .stream()
                .collect(toSet());

        val newTransactions = Sets.difference(allTransactions, existingTransactions);

        return Stream.concat(transactionEntityRepository.saveAll(newTransactions)
                        .stream(), existingTransactions.stream())
                .collect(toSet());
    }

}
