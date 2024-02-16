package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void scheduleIngestion(FilteringParameters fp) {
        log.info("scheduleIngestion, parameters: {}", fp);

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent(fp, "system"));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<String> approveTransactions(List<String> transactionLineIds) {

        return transactionLineIds;
    }

}
