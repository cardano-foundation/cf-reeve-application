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
public class ServiceTxPackaging {
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
                if (ledgerEvent.getDocCode() != null) {
                    eventMetaData.put("docCode", ledgerEvent.getDocCode());
                }
                if (ledgerEvent.getDocDate() != null) {
                    //eventMetaData.put("docDate", Constants.METADATA_DATE_FORMAT.format(ledgerEvent.getDocDate()));
                }
                if (ledgerEvent.getPeriod() != null) {
                    eventMetaData.put("period", ledgerEvent.getPeriod());
                }
                if (ledgerEvent.getVendor() != null) {
                    eventMetaData.put("vendor", ledgerEvent.getVendor());
                }
                if (ledgerEvent.getCostCenter() != null) {
                    eventMetaData.put("costCenter", ledgerEvent.getCostCenter());
                }
                if (ledgerEvent.getProjectCode() != null) {
                    eventMetaData.put("projectCode", ledgerEvent.getProjectCode());
                }
                if (ledgerEvent.getCurrency() != null) {
                    eventMetaData.put("curency", ledgerEvent.getCurrency());
                }
                if (ledgerEvent.getAmount() != null) {
                    eventMetaData.put("amount", ledgerEvent.getAmount());
                }
                if (ledgerEvent.getExchangeRate() != null) {
                    eventMetaData.put("exchangeRate", ledgerEvent.getExchangeRate());
                }
                if (ledgerEvent.getEventCode() != null) {
                    eventMetaData.put("eventCode", ledgerEvent.getEventCode());
                }
                if (ledgerEvent.getEventDescription() != null) {
                    eventMetaData.put("eventDescription", ledgerEvent.getEventDescription());
                }
                if (ledgerEvent.getLineNumber() != null) {
                    eventMetaData.put("lineNumber", ledgerEvent.getLineNumber());
                }
                if (ledgerEvent.getStatus() != null) {
                    eventMetaData.put("status", ledgerEvent.getStatus());
                }
                if (ledgerEvent.getTimestamp() != null) {
                    eventMetaData.put("timestampEvent", ledgerEvent.getTimestamp().toString());
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
