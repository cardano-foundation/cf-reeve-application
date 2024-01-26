package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.IngestionPipelineProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final LedgerService ledgerService;

    private final IngestionPipelineProcessor ingestionPipelineProcessor;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void syncStateFromLedger(Map<String, TransactionLine.LedgerDispatchStatus> statusMap) {
        log.info("syncStateFromLedger...");

        ledgerService.updateTransactionLines(statusMap);
    }

    @Transactional
    public void runIncomingIngestionPipeline(TransactionLines transactionLines) {
        log.info("runIncomingIngestionPipeline...");

        ingestionPipelineProcessor.runPipeline(transactionLines);
    }

    @Transactional
    public void uploadToTheLedger() {
        log.info("uploadToTheLedger...");

        ledgerService.publishLedgerEvents();
    }

    @Transactional
    public void scheduleIngestion(FilteringParameters fp) {
        log.info("scheduleIngestion, parameters: {}", fp);

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent(fp, "system"));
    }

}
