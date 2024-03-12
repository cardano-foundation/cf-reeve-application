package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class TxItemsAggregatorTaskItem implements PipelineTaskItem {

    @Override
    public TransactionWithViolations run(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        Map<TransactionItemKey, List<TransactionItem>> itemsPerKeyMap = tx.getItems()
                .stream()
                .collect(groupingBy(txItem -> TransactionItemKey.builder()
                        .accountCodeCredit(txItem.getAccountCodeCredit())
                        .accountCodeDebit(txItem.getAccountCodeDebit())
                        .accountCodeEventRefCredit(txItem.getAccountCodeEventRefCredit())
                        .accountCodeEventRefDebit(txItem.getAccountCodeEventRefDebit())
                        .accountEventCode(txItem.getAccountEventCode())
                        .build())
                );

        val reducedItems = itemsPerKeyMap.values().stream()
                .map(items -> items.stream()
                        .reduce((a, b) -> a.toBuilder()
                                .amountFcy(a.getAmountFcy().add(b.getAmountFcy()))
                                .amountLcy(a.getAmountLcy().add(b.getAmountLcy()))
                                .build())
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return TransactionWithViolations.create(tx
                .toBuilder()
                .items(reducedItems)
                .build());
    }

    @EqualsAndHashCode
    @Builder
    @Getter
    public static class TransactionItemKey {

        @Builder.Default
        private Optional<String> accountCodeDebit = Optional.empty();

        @Builder.Default
        private Optional<String> accountCodeEventRefDebit = Optional.empty();

        @Builder.Default
        private Optional<String> accountNameDebit = Optional.empty();

        @Builder.Default
        private Optional<String> accountCodeCredit = Optional.empty();

        @Builder.Default
        private Optional<String> accountCodeEventRefCredit = Optional.empty();

        @Builder.Default
        private Optional<String> accountEventCode = Optional.empty();

    }

}
