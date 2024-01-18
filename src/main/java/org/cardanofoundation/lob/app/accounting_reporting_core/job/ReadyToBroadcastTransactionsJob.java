package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.AccountingCoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadyToBroadcastTransactionsJob {

    private final AccountingCoreService accountingCoreService;


    @Scheduled(fixedDelay = 60_000L) // TODO configurable
    public void execute() {
        log.info("Executing ReadyToBroadcastTransactionsJob job...");

        accountingCoreService.publishLedgerEvents();

        log.info("Finished executing ReadyToBroadcastTransactionsJob job.");
    }

}
