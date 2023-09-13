package org.cardanofoundation.lob.txsubmitter.service;

import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.metadata.MetadataList;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.constants.Constants;
import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJob;
import org.cardanofoundation.lob.common.model.TxSubmitJob;
import org.cardanofoundation.lob.common.model.TxSubmitJobStatus;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class TxPackagingService {
    private static final int MAX_EVENTS_PER_TX = 32;

    public List<TxSubmitJob> createTxJobs(final LedgerEventRegistrationJob ledgerEventRegistrationJob) {
        final List<TxSubmitJob> txSubmitJobs = new ArrayList<>();
        final int batches = 1 + ledgerEventRegistrationJob.getLedgerEvents().size() / MAX_EVENTS_PER_TX;
        for (int batchIdx = 0; batchIdx < batches; ++batchIdx) {
            final List<LedgerEvent> batchEvents = ledgerEventRegistrationJob.getLedgerEvents().subList(batchIdx * MAX_EVENTS_PER_TX, Math.min((batchIdx + 1) * MAX_EVENTS_PER_TX, ledgerEventRegistrationJob.getLedgerEvents().size()));
            final Metadata metadata = MetadataBuilder.createMetadata();
            final MetadataMap metadataMap = MetadataBuilder.createMap();
            metadataMap.put("registrationId", ledgerEventRegistrationJob.getRegistrationId());
            final MetadataList metadataList = MetadataBuilder.createList();
            for (final LedgerEvent ledgerEvent : batchEvents) {
                final MetadataMap eventMetaData = MetadataBuilder.createMap();
                if (ledgerEvent.getSourceEventFingerprint() != null) {
                    eventMetaData.put("fingerprint", ledgerEvent.getSourceEventFingerprint());
                }
                if (ledgerEvent.getEntity() != null) {
                    eventMetaData.put("entity", ledgerEvent.getEntity());
                }
                if (ledgerEvent.getModule() != null) {
                    eventMetaData.put("module", ledgerEvent.getModule());
                }
                if (ledgerEvent.getType() != null) {
                    eventMetaData.put("type", ledgerEvent.getType());
                }
                if (ledgerEvent.getDocDate() != null) {
                    //eventMetaData.put("docDate", Constants.METADATA_DATE_FORMAT.format(ledgerEvent.getDocDate()));
                }
                if (ledgerEvent.getBookDate() != null) {
                    eventMetaData.put("bookDate", ledgerEvent.getBookDate());
                }
                if (ledgerEvent.getTransactionNumber() != null) {
                    eventMetaData.put("transactionNumber", ledgerEvent.getTransactionNumber());
                }
                if (ledgerEvent.getDocumentNumber() != null) {
                    eventMetaData.put("documentNumber", ledgerEvent.getDocumentNumber());
                }
                if (ledgerEvent.getEvent() != null) {
                    eventMetaData.put("event", ledgerEvent.getEvent());
                }
                if (ledgerEvent.getCurrency() != null) {
                    eventMetaData.put("currency", ledgerEvent.getCurrency());
                }
                if (ledgerEvent.getAmount() != null) {
                    eventMetaData.put("amount", ledgerEvent.getAmount().toString());
                }
                if (ledgerEvent.getExchangeRate() != null) {
                    eventMetaData.put("exchangeRate", ledgerEvent.getExchangeRate().toString());
                }
                if (ledgerEvent.getCounterParty() != null) {
                    eventMetaData.put("counterParty", ledgerEvent.getCounterParty());
                }
                if (ledgerEvent.getCostCenter() != null) {
                    eventMetaData.put("costCenter", ledgerEvent.getCostCenter());
                }
                if (ledgerEvent.getProjectCode() != null) {
                    eventMetaData.put("projectCode", ledgerEvent.getProjectCode());
                }
                eventMetaData.put("timestamp", BigInteger.valueOf(Instant.now().getEpochSecond()));
                metadataList.add(eventMetaData);
            }
            metadataMap.put("ledgerEvents", metadataList);
            metadata.put(Constants.METADATA_LABEL, metadataMap);

            final TxSubmitJob job = new TxSubmitJob();
            job.setJobStatus(TxSubmitJobStatus.PENDING);
            job.setTransactionMetadata(metadata.serialize());
            txSubmitJobs.add(job);
        }
        return txSubmitJobs;
    }
}
