package org.cardanofoundation.lob.app.blockchain_publisher.service;

import jakarta.persistence.OneToOne;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionItem;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.*;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.STORED;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionConverter {

    private final BlockchainPublishStatusMapper blockchainPublishStatusMapper;

    public TransactionEntity convert(Transaction tx) {
        val transactionEntity = TransactionEntity.builder()
                .id(tx.getId())
                .internalNumber(tx.getInternalTransactionNumber())
                .transactionType(tx.getTransactionType())
                .organisation(convertOrganisation(tx.getOrganisation()))
                .fxRate(tx.getFxRate())
                .entryDate(tx.getEntryDate())
                .projectInternalNumber(tx.getProjectInternalNumber().orElse(null))
                .costCenterInternalNumber(tx.getCostCenterInternalNumber().orElse(null))
                .publishStatus(blockchainPublishStatusMapper.convert(tx.getLedgerDispatchStatus()).orElse(STORED))
                .build();

        transactionEntity.setDocument(convertDocument(tx.getDocument()));
        transactionEntity.setItems(convertTxItems(tx, transactionEntity));

        return transactionEntity;
    }

    private Set<TransactionItemEntity> convertTxItems(Transaction tx, TransactionEntity transactionEntity) {
        return tx.getTransactionItems()
                .stream()
                .map(tl -> convert(transactionEntity, tl))
                .collect(toSet());
    }

    private static Organisation convertOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation org) {
        return Organisation.builder()
                .id(org.getId())
                .currency(Currency.builder()
                        .id(org.getCurrency().getId().orElseThrow()) // currency ref if is mandatory in the organisation
                        .internalNumber(org.getCurrency().getInternalNumber())
                        .build())
                .build();
    }

    private static Document convertDocument(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document doc) {
        val document = new Document();
        document.setInternalDocumentNumber(doc.getInternalNumber());

        document.setCurrency(Currency.builder()
                .id(doc.getCurrency().getId().orElseThrow()) // at this moment we must have currency id
                .internalNumber(doc.getCurrency().getInternalNumber())
                .build());

        document.setVat(doc.getVat().map(vat -> Vat.builder()
                .internalNumber(vat.getInternalNumber())
                .rate(vat.getRate().orElseThrow())
                .build()).orElse(null));

        document.setCounterparty(doc.getCounterparty().map(cp -> Counterparty.builder()
                .internalNumber(cp.getInternalNumber())
                .build()).orElse(null));

        return document;
    }

    @OneToOne
    public TransactionItemEntity convert(TransactionEntity parent,
                                         TransactionItem txLine) {
        return TransactionItemEntity.builder()
                .id(txLine.getId())
                .transaction(parent)
                //.eventCode(null) // TODO
                .amountFcy(txLine.getAmountFcy())
                .build();
    }

}
