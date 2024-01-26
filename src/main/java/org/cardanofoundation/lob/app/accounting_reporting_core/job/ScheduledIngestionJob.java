package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
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

        val fp = FilteringParameters.EMPTY; // TODO if we ever have cron jobs we need to read these from organisation's data

        accountingCoreService.scheduleIngestion(fp);

        log.info("Finished executing ScheduledIngestionJob.");
    }

}
