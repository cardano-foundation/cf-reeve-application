package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class MetadataSerialiser {

    public MetadataMap serialiseToMetadataMap(List<TransactionEntity> transactions,
                                              long creationSlot) {
        val globalMetadataMap = MetadataBuilder.createMap();
        globalMetadataMap.put("metadata", createMetadata(creationSlot));

        val txList = MetadataBuilder.createList();

        transactions.forEach(tx -> txList.add(serialise(tx)));

        globalMetadataMap.put("transactions", txList);

        return globalMetadataMap;
    }

    private static MetadataMap createMetadata(long creationSlot) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("creationSlot", BigInteger.valueOf(creationSlot));

        return metadataMap;
    }

    private static MetadataMap serialise(TransactionEntity transaction) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", transaction.getId());
        metadataMap.put("organisation_id", transaction.getOrganisationId());
        metadataMap.put("tx_type", transaction.getTransactionType().name().toUpperCase());
        metadataMap.put("base_currency_id", transaction.getBaseCurrencyId());
        metadataMap.put("base_currency_internal_code", transaction.getBaseCurrencyInternalCode());
        metadataMap.put("target_currency_id", transaction.getTargetCurrencyId());
        metadataMap.put("target_currency_internal_code", transaction.getTargetCurrencyInternalCode());
        metadataMap.put("entry_date", transaction.getEntryDate().toString());
        metadataMap.put("fx_rate", transaction.getFxRate().toEngineeringString());

        // TODO needs business answers
        //transaction.getDocumentInternalNumber().ifPresent(docInternalNumber -> metadataMap.put("document_internal_number", docInternalNumber));
        transaction.getVatInternalCode().ifPresent(vatInternalCode -> metadataMap.put("vat_internal_code", vatInternalCode));
        transaction.getVatRate().ifPresent(vatRate -> metadataMap.put("vat_rate", vatRate.toEngineeringString()));
        transaction.getVendorInternalCode().ifPresent(vendorInternalCode -> metadataMap.put("vendor_internal_code", vendorInternalCode));

        val txLinesMetadataList = MetadataBuilder.createList();

        for (val txLine : transaction.getLines()) {
            txLinesMetadataList.add(serialise(txLine));
        }

        return metadataMap;
    }

    private static MetadataMap serialise(TransactionLineEntity transactionLineEntity) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", transactionLineEntity.getId());
        metadataMap.put("amount_lcy", transactionLineEntity.getAmountLcy().toEngineeringString());
        metadataMap.put("amount_fcy", transactionLineEntity.getAmountFcy().toEngineeringString());

        return metadataMap;
    }

}
