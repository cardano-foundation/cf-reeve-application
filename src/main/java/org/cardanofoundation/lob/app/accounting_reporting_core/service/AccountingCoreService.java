package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void scheduleIngestion(FilteringParameters fp) {
        log.info("scheduleIngestion, parameters: {}", fp);

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent(fp, "system"));
    }

    @Transactional
    public List<String> approveTransactions(List<String> transactionLineIds) {

        return transactionLineIds;
    }

}
