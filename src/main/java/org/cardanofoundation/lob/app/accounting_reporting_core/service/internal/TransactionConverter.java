package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("accounting_reporting_core.TransactionConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionConverter {

    private final CoreCurrencyService coreCurrencyService;

    public TransactionEntity convert(Transaction transaction) {
        val transactionEntity = new TransactionEntity()
                .id(transaction.getId())
                .transactionInternalNumber(transaction.getInternalTransactionNumber())
                .transactionType(transaction.getTransactionType())
                .entryDate(transaction.getEntryDate())
                .organisation(convertOrganisation(transaction))
                .document(convert(transaction.getDocument()))
                .fxRate(transaction.getFxRate())
                .validationStatus(transaction.getValidationStatus())
                .ledgerDispatchStatus(transaction.getLedgerDispatchStatus())
                .transactionApproved(transaction.isTransactionApproved())
                .ledgerDispatchApproved(transaction.isLedgerDispatchApproved());

        val txItems = transaction.getItems()
                .stream()
                .map(txItemEntity -> {
                    return new TransactionItemEntity()
                            .id(txItemEntity.getId())
                            .transaction(transactionEntity)
                            .amountLcy(txItemEntity.getAmountLcy())
                            .costCenter(convertCostCenter(txItemEntity.getCostCenter()))
                            .amountFcy(txItemEntity.getAmountFcy())
                            .project(convertProject(txItemEntity.getProject()))
                            .accountCodeDebit(txItemEntity.getAccountCodeDebit().orElse(null))
                            .accountCodeRefDebit(txItemEntity.getAccountCodeEventRefDebit().orElse(null))
                            .accountCodeCredit(txItemEntity.getAccountCodeCredit().orElse(null))
                            .accountCodeRefCredit(txItemEntity.getAccountCodeEventRefCredit().orElse(null))
                            .accountNameDebit(txItemEntity.getAccountNameDebit().orElse(null))
                            .accountEventCode(txItemEntity.getAccountEventCode().orElse(null));
                })
                .collect(Collectors.toSet());

        transactionEntity.items(txItems);

        transactionEntity.setCreatedAt(LocalDateTime.now());
        transactionEntity.setUpdatedAt(LocalDateTime.now());
        transactionEntity.setCreatedBy("system");
        transactionEntity.setUpdatedBy("system");

        return transactionEntity;
    }

    private Project convertProject(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project> project) {
        return project.map(p -> Project.builder()
                        .customerCode(p.getCustomerCode())
                        .build())
                .orElse(null);
    }

    private CostCenter convertCostCenter(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter> costCenter) {
        return costCenter.map(cc -> CostCenter.builder()
                        .customerCode(cc.getCustomerCode())
                        .externalCustomerCode(cc.getExternalCustomerCode().orElse(null))
                        .name(cc.getName().orElse(null))
                        .build())
                .orElse(null);
    }

    private static Organisation convertOrganisation(Transaction transaction) {
        return Organisation.builder()
                .id(transaction.getOrganisation().getId())
                .shortName(transaction.getOrganisation().getShortName().orElse(null))
                .currency(transaction.getOrganisation().getCurrency().map(c -> Currency.builder()
                        .id(c.getCoreCurrency().map(CoreCurrency::toExternalId).orElse(null))
                        .customerCode(c.getCustomerCode())
                        .build()).orElse(null))
                .build();
    }

    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document convert(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> docM) {
        return docM.map(doc -> Document.builder()
                        .num(doc.getNumber())

                        .currency(Currency.builder()
                                .customerCode(doc.getCurrency().getCustomerCode())
                                .id(doc.getCurrency().getCoreCurrency().map(CoreCurrency::toExternalId).orElse(null))
                                .build())
                        .vat(doc.getVat().map(vat -> Vat.builder()
                                .customerCode(vat.getCustomerCode())
                                .rate(vat.getRate().orElse(null))
                                .build()).orElse(null))

                        .counterparty(doc.getCounterparty().map(counterparty -> Counterparty.builder()
                                .customerCode(counterparty.getCustomerCode())
                                .type(counterparty.getType())
                                .name(counterparty.getName().orElse(null))
                                .build()).orElse(null)))

                .map(Document.DocumentBuilder::build)
                .orElse(null);
    }

    public Transaction convert(TransactionEntity transactionEntity) {
        // can you write a converter from transactionEntity to transaction

        val items = transactionEntity.items()
                .stream()
                .map(txItemEntity -> TransactionItem.builder()
                        .id(txItemEntity.id())

                        .accountCodeDebit(txItemEntity.getAccountCodeDebit())
                        .accountCodeEventRefDebit(txItemEntity.getAccountCodeRefDebit())

                        .accountCodeCredit(txItemEntity.getAccountCodeCredit())
                        .accountCodeEventRefCredit(txItemEntity.getAccountCodeRefCredit())

                        .accountNameDebit(txItemEntity.getAccountNameDebit())

                        .accountEventCode(txItemEntity.getAccountEventCode())

                        .project(txItemEntity.getProject().map(project -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project.builder()
                                .customerCode(project.getCustomerCode())
                                .build()))

                        .costCenter(txItemEntity.getCostCenter().map(costCenter -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter.builder()
                                .customerCode(costCenter.getCustomerCode())
                                .externalCustomerCode(costCenter.getExternalCustomerCode())
                                .name(costCenter.getName())
                                .build()))

                        .amountFcy(txItemEntity.amountFcy())
                        .amountLcy(txItemEntity.amountLcy())
                        .build())
                .collect(Collectors.toSet());

        return Transaction.builder()
                .id(transactionEntity.id())
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(transactionEntity.organisation().getId())
                        .shortName(transactionEntity.organisation().getShortName())
                        .currency(transactionEntity.organisation().getCurrency().map(c -> {
                            return org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                                    .customerCode(c.getCustomerCode())
                                    .coreCurrency(coreCurrencyService.findByCurrencyId(c.getId().orElseThrow()))
                                    .build();
                        }))
                        .build())
                .document(convert(transactionEntity.document()))
                .entryDate(transactionEntity.entryDate())
                .validationStatus(transactionEntity.validationStatus())
                .transactionType(transactionEntity.transactionType())
                .internalTransactionNumber(transactionEntity.transactionInternalNumber())
                .fxRate(transactionEntity.fxRate())

                .transactionApproved(transactionEntity.transactionApproved())
                .ledgerDispatchStatus(transactionEntity.ledgerDispatchStatus())
                .ledgerDispatchApproved(transactionEntity.ledgerDispatchApproved())
                .items(items)
                .build();
    }

    private Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> convert(@Nullable Document doc) {
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                .number(doc.getNum())
                .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                        .customerCode(doc.getNum())
                        .coreCurrency(doc.getCurrency().getId().flatMap(coreCurrencyService::findByCurrencyId))
                        .build()
                )
                .vat(doc.getVat().map(vat -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat.builder()
                        .customerCode(vat.getCustomerCode())
                        .rate(vat.getRate())
                        .build()))

                .counterparty(doc.getCounterparty().map(counterparty -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.builder()
                        .customerCode(counterparty.getCustomerCode())
                        .name(counterparty.getName())
                        .build()))

                .build());
    }

}
