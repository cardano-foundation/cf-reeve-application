package org.cardanofoundation.lob.app.accounting_reporting_core.service.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    public TransactionEntity convert(Transaction transaction) {
        val transactionEntity = new TransactionEntity();
        transactionEntity.setId(transaction.getId());

        transactionEntity.setTransactionInternalNumber(transaction.getInternalTransactionNumber());
        transactionEntity.setTransactionType(transaction.getTransactionType());
        transactionEntity.setEntryDate(transaction.getEntryDate());

        transactionEntity.setOrganisation(convertOrganisation(transaction));
        transactionEntity.setDocument(convert(transaction.getDocument()));
        transactionEntity.setFxRate(transaction.getFxRate());

        transactionEntity.setCostCenter(convertCostCenter(transaction.getCostCenter()));
        transactionEntity.setProject(convertProject(transaction.getProject()));

        transactionEntity.setValidationStatus(transaction.getValidationStatus());
        transactionEntity.setLedgerDispatchStatus(transaction.getLedgerDispatchStatus());
        transactionEntity.setLedgerDispatchApproved(transaction.isLedgerDispatchApproved());

        val txItems = transaction.getTransactionItems()
                .stream()
                .map(txItemEntity -> {
                    val txItem = new TransactionItemEntity();
                    txItem.setId(txItemEntity.getId());
                    txItem.setTransaction(transactionEntity);
                    txItem.setAmountLcy(txItemEntity.getAmountLcy());
                    txItem.setAmountFcy(txItemEntity.getAmountFcy());
                    txItem.setAccountCodeDebit(txItemEntity.getAccountCodeDebit().orElse(null));
                    txItem.setAccountCodeCredit(txItemEntity.getAccountCodeCredit().orElse(null));
                    txItem.setAccountNameDebit(txItemEntity.getAccountNameDebit().orElse(null));

                    return txItem;
                })
                .collect(Collectors.toSet());

        transactionEntity.setItems(txItems);

        // TODO
        transactionEntity.setCreatedAt(LocalDateTime.now());
        transactionEntity.setUpdatedAt(LocalDateTime.now());
        transactionEntity.setCreatedBy("system");
        transactionEntity.setUpdatedBy("system");

        return transactionEntity;
    }

    private Project convertProject(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project> project) {
        return project.map(p -> Project.builder()
                .internalNumber(p.getInternalNumber())
                .code(p.getCode().orElse(null))
                .build())
                .orElse(null);
    }

    private CostCenter convertCostCenter(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter> costCenter) {
        return costCenter.map(cc -> CostCenter.builder()
                .internalNumber(cc.getInternalNumber())
                .externalNumber(cc.getExternalNumber().orElse(null))
                .code(cc.getCode().orElse(null))
                .build())
                .orElse(null);
    }

    private static Organisation convertOrganisation(Transaction transaction) {
        return Organisation.builder()
                .id(transaction.getOrganisation().getId())
                .currency(Currency.builder()
                        .id(transaction.getOrganisation().getCurrency().getId().orElseThrow()) // currency ref if is mandatory in the organisation
                        .internalNumber(transaction.getOrganisation().getCurrency().getInternalNumber())
                        .build())
                .build();
    }

    private org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Document convert(Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> docM) {
        return docM.map(doc -> Document.builder()
                        .internalNumber(doc.getInternalNumber())

                        .currency(Currency.builder()
                                .id(doc.getCurrency().getId().orElse(null))
                                .internalNumber(doc.getCurrency().getInternalNumber())
                                .build())

                        .vat(doc.getVat().map(vat -> Vat.builder()
                                .internalNumber(vat.getInternalNumber())
                                .rate(vat.getRate().orElse(null))
                                .build()).orElse(null))

                        .counterparty(doc.getCounterparty().map(counterparty -> Counterparty.builder()
                                .internalNumber(counterparty.getInternalNumber())
                                .name(counterparty.getName().orElseThrow())
                                .build()).orElse(null)))

                .map(Document.DocumentBuilder::build)
                .orElse(null);
    }

    public Transaction convert(TransactionEntity transactionEntity) {
        // can you write a converter from transactionEntity to transaction

        val items = transactionEntity.getItems()
                .stream()
                .map(txItemEntity -> TransactionItem.builder()
                        .id(txItemEntity.getId())
                        .accountCodeCredit(txItemEntity.getAccountCodeCredit())
                        .accountCodeDebit(txItemEntity.getAccountCodeDebit())
                        .accountNameDebit(txItemEntity.getAccountNameDebit())
                        .amountFcy(txItemEntity.getAmountFcy())
                        .amountLcy(txItemEntity.getAmountLcy())
                        .build())
                .collect(Collectors.toSet());

        return Transaction.builder()
                .id(transactionEntity.getId())
                .organisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation.builder()
                        .id(transactionEntity.getOrganisation().getId())
                        .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                                .id(transactionEntity.getOrganisation().getCurrency().getId())
                                .internalNumber(transactionEntity.getOrganisation().getCurrency().getInternalNumber())
                                .build())
                        .build())
                .document(convert(transactionEntity.getDocument()))
                .entryDate(transactionEntity.getEntryDate())
                .validationStatus(transactionEntity.getValidationStatus())
                .transactionType(transactionEntity.getTransactionType())
                .internalTransactionNumber(transactionEntity.getTransactionInternalNumber())
                .fxRate(transactionEntity.getFxRate())

                .costCenter(transactionEntity.getCostCenter().map(costCenter -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CostCenter.builder()
                        .internalNumber(costCenter.getInternalNumber())
                        .externalNumber(costCenter.getExternalNumber())
                        .code(costCenter.getCode())
                        .build()))

                .project(transactionEntity.getProject().map(project -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Project.builder()
                        .internalNumber(project.getInternalNumber())
                        .code(project.getCode())
                        .build()))

                .ledgerDispatchStatus(transactionEntity.getLedgerDispatchStatus())
                .ledgerDispatchApproved(transactionEntity.getLedgerDispatchApproved())
                .transactionItems(items)
                .build();
    }

    private static Optional<org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document> convert(@Nullable Document doc) {
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                .internalNumber(doc.getInternalNumber())
                .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                        .id(doc.getCurrency().getId())
                        .internalNumber(doc.getCurrency().getInternalNumber())
                        .build())
                .vat(doc.getVat().map(vat -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat.builder()
                        .internalNumber(vat.getInternalNumber())
                        .rate(vat.getRate())
                        .build()))
                .counterparty(doc.getCounterparty().map(counterparty -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.builder()
                        .internalNumber(counterparty.getInternalNumber())
                        .name(Optional.of(counterparty.getName()))
                        .build()))
                .build());
    }

}
