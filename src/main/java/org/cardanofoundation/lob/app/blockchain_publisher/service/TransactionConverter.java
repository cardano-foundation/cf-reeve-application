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
                .costCenter(tx.getCostCenter().map(cc -> {
                    val ccBuilder = CostCenter.builder();

                    cc.getExternalCustomerCode().ifPresent(ccBuilder::customerCode);
                    cc.getName().ifPresent(ccBuilder::name);

                    return ccBuilder.build();
                }).orElse(null))
                .project(tx.getProject().map(pc -> new Project(pc.getCustomerCode())).orElse(null))
                .l1SubmissionData(L1SubmissionData.builder()
                        .publishStatus(blockchainPublishStatusMapper.convert(tx.getLedgerDispatchStatus()).orElse(STORED))
                        .build())
                .document(convertDocument(tx.getDocument().orElseThrow()))
                .build();

        transactionEntity.setItems(convertTxItems(tx, transactionEntity));

        return transactionEntity;
    }

    private Set<TransactionItemEntity> convertTxItems(Transaction tx, TransactionEntity transactionEntity) {
        return tx.getItems()
                .stream()
                .map(tl -> convert(transactionEntity, tl))
                .collect(toSet());
    }

    private static Organisation convertOrganisation(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation org) {
        return Organisation.builder()
                .id(org.getId())
                .shortName(org.getShortName().orElseThrow())
                .currency(new Currency(org.getCurrency().orElseThrow().getCoreCurrency().orElseThrow().toExternalId()))
                .build();
    }

    private static Document convertDocument(org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Document doc) {
        return new Document()
                .num(doc.getNumber())
                .currency(Currency.builder()
                        .id(doc.getCurrency().getCoreCurrency().orElseThrow().toExternalId())
                        .build()
                )
                .vat(doc.getVat().map(vat -> Vat.builder()
                        .rate(vat.getRate().orElseThrow())
                        .build()).orElse(null))
                .counterparty(doc.getCounterparty().map(cp -> Counterparty.builder()
                        .customerCode(cp.getCustomerCode())
                        .type(cp.getType())
                        .build()).orElse(null));
    }

    @OneToOne
    public TransactionItemEntity convert(TransactionEntity parent,
                                         TransactionItem txLine) {
        return TransactionItemEntity.builder()
                .id(txLine.getId())
                .transaction(parent)
                .eventCode(txLine.getAccountEventCode().orElse(null))
                .amountFcy(txLine.getAmountFcy())
                .build();
    }

}
