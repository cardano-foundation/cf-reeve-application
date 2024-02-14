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
import static org.cardanofoundation.lob.app.blockchain_publisher.util.SHA3.digestAsBase64;

@Service
@Slf4j
public class TransactionLineConverter {

    public TransactionEntity convert(Transaction tx) {
        // TODO this is hacky but currently core model is way too clunky
        val txLine = tx.getTransactionLines().stream().findAny().orElseThrow();

        val organisationId = tx.getOrgTransactionNumber().organisationId();
        val txInternalNumber = tx.getOrgTransactionNumber().transactionNumber();

        val t = TransactionEntity.builder()
                .id(digestAsBase64(STR."\{organisationId}::\{ txInternalNumber }"))
                .internalNumber(txInternalNumber)
                .transactionType(txLine.getTransactionType())
                .organisation(new Organisation(organisationId, Currency.builder()
                        .id(txLine.getOrganisationCurrencyId())
                        .internalCode(txLine.getOrganisationCurrencyInternalId())
                        .build()))
                .fxRate(txLine.getFxRate())
                .entryDate(txLine.getEntryDate())
                .projectInternalCode(txLine.getProjectInternalCode().orElse(null))
                .costCenterInternalCode(txLine.getCostCenterInternalCode().orElse(null))
                .publishStatus(STORED)
                .build();

        val txEntityItems = tx.getTransactionLines()
                .stream()
                .map(tl -> convert(t, tl))
                .collect(toSet());

        t.setDocument(convertDocument(txLine));

        t.setItems(txEntityItems);

        return t;
    }

    private static Document convertDocument(TransactionLine txLine) {
        val document = new Document();
        document.setInternalDocumentNumber(txLine.getDocumentInternalNumber());

        document.setCurrency(Currency.builder()
                .id(txLine.getDocumentCurrencyId().orElseThrow()) // at this moment we must have currency id
                .internalCode(txLine.getDocumentCurrencyInternalId())
                .build());

        if (txLine.getDocumentVatInternalCode().isPresent() && txLine.getDocumentVatRate().isPresent()) {
            document.setVat(Vat.builder()
                    .internalCode(txLine.getDocumentVatInternalCode().orElseThrow())
                    .rate(txLine.getDocumentVatRate().orElseThrow())
                    .build()
            );
        }

        if (txLine.getCounterpartyInternalNumber().isPresent()) {
            val counterparty = new Counterparty(txLine.getCounterpartyInternalNumber().orElseThrow());
            document.setCounterparty(counterparty);
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
