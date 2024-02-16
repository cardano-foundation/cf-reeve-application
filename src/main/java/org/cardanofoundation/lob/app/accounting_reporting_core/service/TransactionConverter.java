package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.springframework.stereotype.Service;

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

        transactionEntity.setOrganisation(Organisation.builder()
                .id(transaction.getOrganisation().getId())
                .currency(Currency.builder()
                        .id(transaction.getOrganisation().getCurrency().getId().orElseThrow()) // currency ref if is mandatory in the organisation
                        .internalNumber(transaction.getOrganisation().getCurrency().getInternalNumber())
                        .build())
                .build());

        transactionEntity.setDocument(Document.builder()
                .internalNumber(transaction.getDocument().getInternalNumber())

                .currency(Currency.builder()
                        .id(transaction.getDocument().getCurrency().getId().orElse(null))
                        .internalNumber(transaction.getDocument().getCurrency().getInternalNumber())
                        .build())

                .vat(transaction.getDocument().getVat().map(vat -> Vat.builder()
                        .internalNumber(vat.getInternalNumber())
                        .rate(vat.getRate().orElseThrow())
                        .build()).orElse(null))

                .counterparty(transaction.getDocument().getCounterparty().map(counterparty -> Counterparty.builder()
                        .internalNumber(counterparty.getInternalNumber())
                        .name(counterparty.getName().orElseThrow())
                        .build()).orElse(null))

                .build());

        transactionEntity.setFxRate(transaction.getFxRate());

        transactionEntity.setCostCenterInternalNumber(transaction.getCostCenterInternalNumber().orElse(null));
        transactionEntity.setProjectInternalNumber(transaction.getProjectInternalNumber().orElse(null));
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
                .document(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document.builder()
                        .internalNumber(transactionEntity.getDocument().getInternalNumber())
                        .currency(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency.builder()
                                .id(transactionEntity.getDocument().getCurrency().getId())
                                .internalNumber(transactionEntity.getDocument().getCurrency().getInternalNumber())
                                .build())
                        .vat(transactionEntity.getDocument().getVat().map(vat -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Vat.builder()
                                .internalNumber(vat.getInternalNumber())
                                .rate(vat.getRate())
                                .build()))
                        .counterparty(transactionEntity.getDocument().getCounterparty().map(counterparty -> org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Counterparty.builder()
                                .internalNumber(counterparty.getInternalNumber())
                                .name(Optional.of(counterparty.getName()))
                                .build()))
                        .build())

                .entryDate(transactionEntity.getEntryDate())
                .validationStatus(transactionEntity.getValidationStatus())
                .transactionType(transactionEntity.getTransactionType())
                .internalTransactionNumber(transactionEntity.getTransactionInternalNumber())
                .fxRate(transactionEntity.getFxRate())
                .costCenterInternalNumber(transactionEntity.getCostCenterInternalNumber())
                .projectInternalNumber(transactionEntity.getProjectInternalNumber())
                .ledgerDispatchStatus(transactionEntity.getLedgerDispatchStatus())
                .ledgerDispatchApproved(transactionEntity.getLedgerDispatchApproved())
                .transactionItems(items)
                .build();
    }

}
