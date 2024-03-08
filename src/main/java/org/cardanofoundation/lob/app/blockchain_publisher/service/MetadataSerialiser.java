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

        globalMetadataMap.put("txs", txList);

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
        metadataMap.put("num", transaction.getInternalNumber());

        metadataMap.put("type", transaction.getTransactionType().name());

        transaction.getCostCenter().ifPresent(costCenter -> metadataMap.put("cost_center", serialise(costCenter)));
        transaction.getProject().ifPresent(project -> metadataMap.put("project", serialise(project)));

        metadataMap.put("date", transaction.getEntryDate().toString());
        metadataMap.put("fx_rate", transaction.getFxRate().toEngineeringString());
        metadataMap.put("org", serialise(transaction.getOrganisation()));

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
        metadataMap.put("code", costCenter.getCustomerCode());
        metadataMap.put("name", costCenter.getName());

        return metadataMap;
    }

    private static MetadataMap serialise(Project project) {
        val metadataMap = MetadataBuilder.createMap();
        metadataMap.put("code", project.getCustomerCode());

        return metadataMap;
    }

    private static MetadataMap serialise(Document document) {
        val metadataMap = MetadataBuilder.createMap();

        metadataMap.put("number", document.num());
        metadataMap.put("currency", serialise(document.currency()));

        document.getVat().ifPresent(vat -> metadataMap.put("vat", serialise(vat)));
        document.getCounterparty().ifPresent(counterparty -> metadataMap.put("counterparty", serialiseCounterparty(counterparty)));


        return metadataMap;
    }

    private static MetadataMap serialiseCounterparty(Counterparty counterparty) {
        val counterpartyMap = MetadataBuilder.createMap();
        counterpartyMap.put("code", counterparty.getCustomerCode());
        counterpartyMap.put("type", counterparty.getType().name());

        return counterpartyMap;
    }

    private static MetadataMap serialise(Currency currency) {
        val metadataMap = MetadataBuilder.createMap();
        metadataMap.put("id", currency.getId());

        return metadataMap;
    }

    private static MetadataMap serialise(Vat vat) {
        val vatMetadataMap = MetadataBuilder.createMap();
        vatMetadataMap.put("rate", vat.getRate().toEngineeringString());

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
        metadataMap.put("short_name", org.getShortName());
        // send VAT_ID to the blockchain
        metadataMap.put("currency", serialise(org.getCurrency()));

        return metadataMap;
    }

}
