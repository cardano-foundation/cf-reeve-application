package org.cardanofoundation.lob.app.blockchain_publisher.service;

import jakarta.persistence.OneToOne;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.DocumentEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.Vat;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.STORED;

@Service
@Slf4j
public class TransactionLineConverter {

    public TransactionEntity convert(UUID uploadId, Transaction tx) {
        // TODO this is hacky but currently core model is way too clunky
        val txLine = tx.getTransactionLines().stream().findAny().orElseThrow();

        val t = TransactionEntity.builder()
                .id(tx.getTransactionNumber())
                .transactionType(txLine.getTransactionType())
                .organisationId(txLine.getOrganisationId())
                .baseCurrencyId(txLine.getBaseCurrencyId())
                .baseCurrencyInternalCode(txLine.getBaseCurrencyInternalId())
                .targetCurrencyId(txLine.getTargetCurrencyId().orElseThrow())
                .targetCurrencyInternalCode(txLine.getTargetCurrencyInternalId())
                .fxRate(txLine.getFxRate())
                .entryDate(txLine.getEntryDate())
                .publishStatus(STORED)
                .build();

        val txLineEntities = tx.getTransactionLines()
                .stream()
                .map(tl -> convert(t, tl))
                .collect(Collectors.toSet());

        if (txLine.getInternalDocumentNumber().isPresent()) {
            t.setDocument(convertDocument(txLine.getInternalDocumentNumber().orElseThrow(), t, txLine));
        }

        t.setLines(txLineEntities);

        return t;
    }

    private static DocumentEntity convertDocument(String internalDocumentNumber,
                                                  TransactionEntity t,
                                                  TransactionLine txLine) {
        val document = new DocumentEntity();
        document.setId(internalDocumentNumber);
        document.setTransaction(t);
        txLine.getInternalDocumentNumber().ifPresent(document::setId);

        if (txLine.getVatInternalCode().isPresent() && txLine.getVatRate().isPresent()) {
            document.setVat(Vat.builder()
                    .internalCode(txLine.getVatInternalCode().get())
                    .rate(txLine.getVatRate().get())
                    .build()
            );
        }

        if (txLine.getInternalVendorCode().isPresent()) {
            document.setVendorInternalCode(txLine.getInternalVendorCode().get());
        }

        return document;
    }

    @OneToOne
    public TransactionItemEntity convert(TransactionEntity parent,
                                         TransactionLine tx) {
        return TransactionItemEntity.builder()
                .id(tx.getId())
                .transaction(parent)
                .amountFcy(tx.getAmountFcy())
                .build();
    }

}
