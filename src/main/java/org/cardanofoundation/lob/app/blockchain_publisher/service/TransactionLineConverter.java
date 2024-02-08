package org.cardanofoundation.lob.app.blockchain_publisher.service;

import jakarta.persistence.OneToOne;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
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
                .vatRate(txLine.getVatRate().orElse(null))
                .vatInternalCode(txLine.getVatInternalCode().orElse(null))
                .vendorInternalCode(txLine.getInternalVendorCode().orElse(null))
                //.documentInternalNumber(txLine.getInternalTransactionNumber())
                .publishStatus(STORED)
                .build();

        val txLineEntities = tx.getTransactionLines()
                .stream()
                .map(tl -> convert(t, tl))
                .collect(Collectors.toSet());

        t.setLines(txLineEntities);

        return t;
    }

    @OneToOne
    public TransactionLineEntity convert(TransactionEntity parent, TransactionLine tx) {
        return TransactionLineEntity.builder()
                .id(tx.getId())
                .transaction(parent)
                .amountFcy(tx.getAmountFcy())
                .amountLcy(tx.getAmountLcy())
                .build();
    }

}
