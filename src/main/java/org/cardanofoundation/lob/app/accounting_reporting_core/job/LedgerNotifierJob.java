package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.AccountingCoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LedgerNotifierJob {

    private final AccountingCoreService accountingCoreService;

    @Scheduled(fixedDelayString = "PT1M")
    public void execute() {
        log.info("Executing LedgerNotifierJob job...");

        accountingCoreService.publishLedgerEvents();

        log.info("Finished executing LedgerNotifierJob job.");
    }

}
