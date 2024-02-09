package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.DocumentEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.Vat;
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

        metadataMap.put("internal_tx_number", transaction.getId().getTransactionInternalNumber());
        metadataMap.put("organisation_id", transaction.getId().getOrganisationId());
        metadataMap.put("tx_type", transaction.getTransactionType().name().toUpperCase());

        metadataMap.put("base_currency_id", transaction.getBaseCurrencyId());
        metadataMap.put("base_currency_internal_code", transaction.getBaseCurrencyInternalCode());

        metadataMap.put("target_currency_id", transaction.getTargetCurrencyId());
        metadataMap.put("target_currency_internal_code", transaction.getTargetCurrencyInternalCode());

        metadataMap.put("entry_date", transaction.getEntryDate().toString());
        metadataMap.put("fx_rate", transaction.getFxRate().toEngineeringString());

        val documentsList = MetadataBuilder.createList();
        transaction.getDocument().ifPresent(doc -> documentsList.add(serialise(doc)));

        metadataMap.put("documents", documentsList);

        val txLinesMetadataList = MetadataBuilder.createList();

        for (val txLine : transaction.getItems()) {
            txLinesMetadataList.add(serialise(txLine));
        }

        metadataMap.put("items", txLinesMetadataList);

        return metadataMap;
    }

    private static MetadataMap serialise(DocumentEntity documentEntity) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", documentEntity.getId());

        documentEntity.getVat().ifPresent(vat -> metadataMap.put("vat", serialise(vat)));
        documentEntity.getVendorInternalCode().ifPresent(vendorInternalCode -> metadataMap.put("vendor_internal_code", vendorInternalCode));

        return metadataMap;
    }

    private static MetadataMap serialise(Vat vat) {
        val vatMetadataMap = MetadataBuilder.createMap();
        vatMetadataMap.put("internal_code", vat.getInternalCode());
        vatMetadataMap.put("rate", vat.getRate().toEngineeringString());

        return vatMetadataMap;
    }

    private static MetadataMap serialise(TransactionItemEntity transactionItemEntity) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", transactionItemEntity.getId());
        metadataMap.put("amount_fcy", transactionItemEntity.getAmountFcy().toEngineeringString());

        return metadataMap;
    }

}
