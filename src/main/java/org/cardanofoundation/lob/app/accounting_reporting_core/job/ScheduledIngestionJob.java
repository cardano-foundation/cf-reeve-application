package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.AccountingCoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledIngestionJob {

    private final AccountingCoreService accountingCoreService;

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT10S")
    public void execute() {
        log.info("Executing ScheduledIngestionJob...");

        accountingCoreService.scheduleIngestion();

        log.info("Finished executing ScheduledIngestionJob.");
    }

}
