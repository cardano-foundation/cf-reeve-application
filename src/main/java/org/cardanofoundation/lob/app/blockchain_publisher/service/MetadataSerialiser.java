package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.Document;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionItemEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.Vat;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class MetadataSerialiser {

    public MetadataMap serialiseToMetadataMap(String organisationId,
                                              List<TransactionEntity> transactions,
                                              long creationSlot) {
        val globalMetadataMap = MetadataBuilder.createMap();
        globalMetadataMap.put("metadata", createMetadata(creationSlot));

        val txList = MetadataBuilder.createList();

        transactions.forEach(tx -> txList.add(serialise(tx)));

        val organisationMap = MetadataBuilder.createMap();
        organisationMap.put("id", organisationId);

        globalMetadataMap.put("transactions", txList);
        globalMetadataMap.put("organisation", organisationMap);

        return globalMetadataMap;
    }

    private static MetadataMap createMetadata(long creationSlot) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("creation_slot", BigInteger.valueOf(creationSlot));

        return metadataMap;
    }

    private static MetadataMap serialise(TransactionEntity transaction) {
        val metadataMap = MetadataBuilder.createMap();

        val id = transaction.getId();

        metadataMap.put("id", transaction.getId().id());
        metadataMap.put("internal_number", id.getTransactionInternalNumber());

        metadataMap.put("type", transaction.getTransactionType().name().toUpperCase());

        val baseCurrencyMap = serialise(transaction.getBaseCurrencyInternalCode(), transaction.getBaseCurrencyId());
        val targetCurrencyMap = serialise(transaction.getTargetCurrencyInternalCode(), transaction.getTargetCurrencyId());

        transaction.getCostCenterInternalCode().ifPresent(costCenter -> metadataMap.put("cost_center", costCenter));
        transaction.getProjectInternalCode().ifPresent(project -> metadataMap.put("project_code", project));

        metadataMap.put("date", transaction.getEntryDate().toString());
        metadataMap.put("fx_rate", transaction.getFxRate().toEngineeringString());

        val documentsList = MetadataBuilder.createList();
        transaction.getDocument().ifPresent(doc -> documentsList.add(serialise(doc)));

        metadataMap.put("base_currency", baseCurrencyMap);
        metadataMap.put("target_currency", targetCurrencyMap);

        if (documentsList.size() > 0) {
            metadataMap.put("documents", documentsList);
        }

        val txLinesMetadataList = MetadataBuilder.createList();

        for (val txLine : transaction.getItems()) {
            txLinesMetadataList.add(serialise(txLine));
        }

        if (txLinesMetadataList.size() > 0) {
            metadataMap.put("items", txLinesMetadataList);
        }

        return metadataMap;
    }

    private static MetadataMap serialise(String currencyCode, String currencyId) {
        val currencyMetadataMap = MetadataBuilder.createMap();

        currencyMetadataMap.put("id", currencyId);
        currencyMetadataMap.put("internal_code", currencyCode);

        return currencyMetadataMap;
    }

    private static MetadataMap serialise(Document document) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("number", document.getInternalDocumentNumber());

        document.getVat().ifPresent(vat -> metadataMap.put("vat", serialise(vat)));

        val counterpartyMap = MetadataBuilder.createMap();
        document.getCounterparty().ifPresent(counterparty -> counterpartyMap.put("internal_code", counterparty.getInternalCode()));

        if (!counterpartyMap.keys().isEmpty()) {
            metadataMap.put("counterparty", counterpartyMap);
        }

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
        metadataMap.put("amount", transactionItemEntity.getAmountFcy().toEngineeringString());

        return metadataMap;
    }

}
