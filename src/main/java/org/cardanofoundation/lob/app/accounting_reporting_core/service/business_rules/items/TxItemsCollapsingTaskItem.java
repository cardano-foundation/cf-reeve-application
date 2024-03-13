package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class TxItemsCollapsingTaskItem implements PipelineTaskItem {

    @Override
    public TransactionWithViolations run(TransactionWithViolations transactionWithViolations) {
        val tx = transactionWithViolations.transaction();

        if (tx.getValidationStatus() == FAILED) {
            return transactionWithViolations;
        }

        Map<TransactionItemKey, List<TransactionItem>> itemsPerKeyMap = tx.getItems()
                .stream()
                .collect(groupingBy(txItem -> TransactionItemKey.builder()
                        .costCenterCode(txItem.getCostCenter().flatMap(CostCenter::getExternalCustomerCode))
                        .documentNumber(txItem.getDocument().map(Document::getNumber))
                        
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

//        @Builder.Default
//        private Optional<String> accountCodeDebit = Optional.empty();
//
//        @Builder.Default
//        private Optional<String> accountCodeEventRefDebit = Optional.empty();
//
//        @Builder.Default
//        private Optional<String> accountNameDebit = Optional.empty();
//
//        @Builder.Default
//        private Optional<String> accountCodeCredit = Optional.empty();
//
//        @Builder.Default
//        private Optional<String> accountCodeEventRefCredit = Optional.empty();

        @Builder.Default
        private Optional<String> costCenterCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentNumber = Optional.empty();

        @Builder.Default
        private Optional<String> accountEventCode = Optional.empty();

    }

}
