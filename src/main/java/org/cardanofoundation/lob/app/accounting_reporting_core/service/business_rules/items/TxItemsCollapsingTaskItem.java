package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;

@Slf4j
public class TxItemsCollapsingTaskItem implements PipelineTaskItem {

    @Override
    public Transaction run(Transaction tx) {
        if (tx.getValidationStatus() == FAILED) {
            return tx;
        }

        val itemsPerKeyMap = tx.getItems()
                .stream()
                .collect(groupingBy(txItem -> TransactionItemKey.builder()
                        .costCenterCustomerCode(txItem.getCostCenter().flatMap(CostCenter::getExternalCustomerCode))
                        .documentVatCustomerCode(txItem.getDocument().flatMap(d -> d.getVat().map(Vat::getCustomerCode)))
                        .documentNum(txItem.getDocument().map(Document::getNumber))
                        .documentCurrencyId(txItem.getDocument().flatMap(d -> d.getCurrency().getCoreCurrency().map(CoreCurrency::toExternalId)))
                        .documentCounterpartyCustomerCode(txItem.getDocument().flatMap(d -> d.getCounterparty().map(Counterparty::getCustomerCode)))
                        .accountEventCode(txItem.getAccountEventCode())
                        .build())
                );

        val txItems = itemsPerKeyMap.values().stream()
                .map(items -> items.stream()
                        .reduce((txItem1, txItem2) -> txItem1.toBuilder()
                                .amountFcy(txItem1.getAmountFcy().add(txItem2.getAmountFcy()))
                                .amountLcy(txItem1.getAmountLcy().add(txItem2.getAmountLcy()))
                                .build())
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return tx.toBuilder()
                .items(txItems)
                .build();
    }

    @EqualsAndHashCode
    @Builder
    @Getter
    public static class TransactionItemKey {

        @Builder.Default
        private Optional<String> costCenterCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentNum = Optional.empty();

        @Builder.Default
        private Optional<String> documentVatCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentCounterpartyCustomerCode = Optional.empty();

        @Builder.Default
        private Optional<String> documentCurrencyId = Optional.empty();

        @Builder.Default
        private Optional<String> accountEventCode = Optional.empty();

    }

}
