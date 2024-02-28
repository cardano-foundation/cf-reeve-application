package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.*;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Set;

@Service
public class MetadataSerialiser {

    public MetadataMap serialiseToMetadataMap(String organisationId,
                                              Set<TransactionEntity> transactions,
                                              long creationSlot) {
        val globalMetadataMap = MetadataBuilder.createMap();
        globalMetadataMap.put("metadata", createMetadata(creationSlot));

        val txList = MetadataBuilder.createList();

        transactions.forEach(tx -> txList.add(serialise(tx)));

        val organisationMap = MetadataBuilder.createMap();
        organisationMap.put("id", organisationId);

        globalMetadataMap.put("transactions", txList);

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

        metadataMap.put("id", id);
        metadataMap.put("internal_number", transaction.getInternalNumber());

        metadataMap.put("type", transaction.getTransactionType().name());

        transaction.getCostCenter().ifPresent(costCenter -> metadataMap.put("cost_center", serialise(costCenter)));
        transaction.getProject().ifPresent(project -> metadataMap.put("project", serialise(project)));

        metadataMap.put("date", transaction.getEntryDate().toString());
        metadataMap.put("fx_rate", transaction.getFxRate().toEngineeringString());
        metadataMap.put("organisation", serialise(transaction.getOrganisation()));

        val documentsList = MetadataBuilder.createList();
        documentsList.add(serialise(transaction.getDocument()));

        if (documentsList.size() > 0) {
            metadataMap.put("documents", documentsList);
        }

        val transactionItemsMetadataList = MetadataBuilder.createList();

        for (val txLine : transaction.getItems()) {
            transactionItemsMetadataList.add(serialise(txLine));
        }

        if (transactionItemsMetadataList.size() > 0) {
            metadataMap.put("items", transactionItemsMetadataList);
        }

        return metadataMap;
    }

    private static MetadataMap serialise(CostCenter costCenter) {
        val metadataMap = MetadataBuilder.createMap();
        metadataMap.put("code", costCenter.getCode());

        return metadataMap;
    }

    private static MetadataMap serialise(Project project) {
        val metadataMap = MetadataBuilder.createMap();
//        metadataMap.put("internal_number", project.getInternalNumber());
        metadataMap.put("code", project.getCode());

        return metadataMap;
    }

    private static MetadataMap serialise(Document document) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("number", document.getInternalDocumentNumber());

        document.getVat().ifPresent(vat -> metadataMap.put("vat", serialise(vat)));

        val counterpartyMap = MetadataBuilder.createMap();
        document.getCounterparty().ifPresent(counterparty -> counterpartyMap.put("internal_number", counterparty.getInternalNumber()));

        if (!counterpartyMap.keys().isEmpty()) {
            metadataMap.put("counterparty", counterpartyMap);
        }

        metadataMap.put("currency", serialise(document.getCurrency()));

        return metadataMap;
    }

    private static MetadataMap serialise(Currency currency) {
        val metadataMap = MetadataBuilder.createMap();
        metadataMap.put("id", currency.getId());
        //metadataMap.put("internal_number", currency.getInternalNumber());

        return metadataMap;
    }

    private static MetadataMap serialise(Vat vat) {
        val vatMetadataMap = MetadataBuilder.createMap();
        vatMetadataMap.put("rate", vat.getRate().toEngineeringString());
        //vatMetadataMap.put("internal_number", vat.getInternalNumber());

        return vatMetadataMap;
    }

    private static MetadataMap serialise(TransactionItemEntity transactionItemEntity) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", transactionItemEntity.getId());
        metadataMap.put("amount", transactionItemEntity.getAmountFcy().toEngineeringString());
        transactionItemEntity.getEventCode().ifPresent(eventCode -> metadataMap.put("event_code", eventCode));

        return metadataMap;
    }

    private static MetadataMap serialise(Organisation org) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("id", org.getId());
        metadataMap.put("currency", serialise(org.getCurrency()));

        return metadataMap;
    }

}
