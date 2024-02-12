package org.cardanofoundation.lob.app.blockchain_publisher.service;

import jakarta.persistence.OneToOne;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.*;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toSet;
import static org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus.STORED;

@Service
@Slf4j
public class TransactionLineConverter {

    public TransactionEntity convert(Transaction tx) {
        // TODO this is hacky but currently core model is way too clunky
        val txLine = tx.getTransactionLines().stream().findAny().orElseThrow();

        val t = TransactionEntity.builder()
                .id(new TransactionId(tx.getOrgTransactionNumber().organisationId(), tx.getOrgTransactionNumber().transactionNumber()))
                .transactionType(txLine.getTransactionType())
                .baseCurrencyId(txLine.getBaseCurrencyId())
                .baseCurrencyInternalCode(txLine.getBaseCurrencyInternalId())
                .targetCurrencyId(txLine.getTargetCurrencyId().orElseThrow())
                .targetCurrencyInternalCode(txLine.getTargetCurrencyInternalId())
                .fxRate(txLine.getFxRate())
                .entryDate(txLine.getEntryDate())
                .projectInternalCode(txLine.getInternalProjectCode().orElse(null))
                .costCenterInternalCode(txLine.getInternalCostCenterCode().orElse(null))
                .publishStatus(STORED)
                .build();

        val txEntityItems = tx.getTransactionLines()
                .stream()
                .map(tl -> convert(t, tl))
                .collect(toSet());

        if (txLine.getInternalDocumentNumber().isPresent()) {
            t.setDocument(convertDocument(txLine.getInternalDocumentNumber().orElseThrow(), txLine));
        }

        t.setItems(txEntityItems);

        return t;
    }

    private static Document convertDocument(String internalDocumentNumber,
                                            TransactionLine txLine) {
        val document = new Document();
        document.setInternalDocumentNumber(internalDocumentNumber);

        if (txLine.getVatInternalCode().isPresent() && txLine.getVatRate().isPresent()) {
            document.setVat(Vat.builder()
                    .internalCode(txLine.getVatInternalCode().orElseThrow())
                    .rate(txLine.getVatRate().orElseThrow())
                    .build()
            );
        }

        if (txLine.getInternalVendorCode().isPresent()) {
            document.setVendorInternalCode(txLine.getInternalVendorCode().orElseThrow());
        }

        return document;
    }

    @OneToOne
    public TransactionItemEntity convert(TransactionEntity parent,
                                         TransactionLine txLine) {
        return TransactionItemEntity.builder()
                .id(txLine.getId())
                .transaction(parent)
                //.eventCode(null) // TODO
                .amountFcy(txLine.getAmountFcy())
                .build();
    }

}
