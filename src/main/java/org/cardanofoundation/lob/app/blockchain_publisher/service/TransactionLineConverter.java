package org.cardanofoundation.lob.app.blockchain_publisher.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.core.BlockchainPublishStatus;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class TransactionLineConverter {

    public TransactionLineEntity convert(UUID uploadId, TransactionLine txLine) {
        val entityTxLine = new TransactionLineEntity();

        entityTxLine.setId(txLine.getId());
        entityTxLine.setOrganisationId(txLine.getOrganisationId());
        entityTxLine.setTransactionType(txLine.getTransactionType());
        entityTxLine.setEntryDate(txLine.getEntryDate());
        entityTxLine.setTransactionInternalNumber(txLine.getInternalTransactionNumber());
        entityTxLine.setUploadId(uploadId);

        entityTxLine.setBaseCurrencyInternalCode(txLine.getBaseCurrencyInternalId());
        entityTxLine.setBaseCurrencyId(txLine.getBaseCurrencyId());

        entityTxLine.setTargetCurrencyInternalCode(txLine.getTargetCurrencyInternalId());
        entityTxLine.setTargetCurrencyId(txLine.getTargetCurrencyId().orElseThrow());

        entityTxLine.setFxRate(txLine.getFxRate());

        entityTxLine.setDocumentInternalNumber(txLine.getInternalTransactionNumber());

        entityTxLine.setVendorInternalCode(txLine.getInternalVendorCode().orElse(null));

        entityTxLine.setVatInternalCode(txLine.getVatInternalCode().orElse(null));
        entityTxLine.setVatRate(txLine.getVatRate().orElse(null));

        entityTxLine.setAmountFcy(txLine.getAmountFcy());
        entityTxLine.setAmountLcy(txLine.getAmountLcy());
        entityTxLine.setPublishStatus(BlockchainPublishStatus.STORED);

        return entityTxLine;
    }

}
