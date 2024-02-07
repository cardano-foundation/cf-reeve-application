package org.cardanofoundation.lob.app.blockchain_publisher.service;

import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.blockchain_publisher.util.WithExtraIds;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

@Service
public class MetadataSerialiser {

    public WithExtraIds<MetadataMap> serialiseToMetadataMap(List<TransactionLineEntity> transactionLines,
                                                                  long slot) {
        val globalMetadataMap = MetadataBuilder.createMap();
        globalMetadataMap.put("creationSlot", BigInteger.valueOf(slot));

        val txLineIds = new HashSet<String>();

        val txList = MetadataBuilder.createList();

        transactionLines.forEach(txLine -> {
            txList.add(serialise(txLine));
            txLineIds.add(txLine.getId());
        });

        globalMetadataMap.put("transactions", txList);

        return new WithExtraIds<>(txLineIds, globalMetadataMap);
    }

    private static MetadataMap serialise(TransactionLineEntity transactionLine) {
        val txLineMetadataMap = MetadataBuilder.createMap();

        txLineMetadataMap.put("id", transactionLine.getId());
        txLineMetadataMap.put("organisation_id", transactionLine.getOrganisationId());
        txLineMetadataMap.put("tx_type", transactionLine.getTransactionType().name().toUpperCase());
        txLineMetadataMap.put("amount_lcy", transactionLine.getAmountLcy().toEngineeringString()); // keep it short
        txLineMetadataMap.put("amount_fcy", transactionLine.getAmountFcy().toEngineeringString()); // keep it short
        txLineMetadataMap.put("base_currency_id", transactionLine.getBaseCurrencyId());
        txLineMetadataMap.put("base_currency_internal_code", transactionLine.getBaseCurrencyInternalCode());
        txLineMetadataMap.put("target_currency_id", transactionLine.getTargetCurrencyId());
        txLineMetadataMap.put("target_currency_internal_code", transactionLine.getTargetCurrencyInternalCode());
        txLineMetadataMap.put("entry_date", transactionLine.getEntryDate().toString());
        txLineMetadataMap.put("fx_rate", transactionLine.getFxRate().toEngineeringString());

        transactionLine.getTransactionInternalNumber().ifPresent(txInternalNumber -> txLineMetadataMap.put("transaction_internal_number", txInternalNumber));
        transactionLine.getDocumentInternalNumber().ifPresent(docInternalNumber -> txLineMetadataMap.put("document_internal_number", docInternalNumber));
        transactionLine.getVatInternalCode().ifPresent(vatInternalCode -> txLineMetadataMap.put("vat_internal_code", vatInternalCode));
        transactionLine.getVatRate().ifPresent(vatRate -> txLineMetadataMap.put("vat_rate", vatRate.toEngineeringString()));
        transactionLine.getVendorInternalCode().ifPresent(vendorInternalCode -> txLineMetadataMap.put("vendor_internal_code", vendorInternalCode));

        return txLineMetadataMap;
    }

}
