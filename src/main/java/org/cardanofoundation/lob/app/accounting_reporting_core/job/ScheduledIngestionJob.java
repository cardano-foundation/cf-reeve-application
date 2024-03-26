package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledIngestionJob {

    private final AccountingCoreService accountingCoreService;

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT10S")
    public void execute() {
        log.info("Executing ScheduledIngestionJob...");

        val fp = UserExtractionParameters.builder()
                .to(LocalDate.now())
                .from(LocalDate.now().minusYears(20))
                .organisationId("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                .transactionTypes(List.of(TransactionType.CardCharge, TransactionType.FxRevaluation))
                //.transactionNumbers(List.of("JOURNAL226", "JOURNAL227"))
                .build();

        accountingCoreService.scheduleIngestion(fp);

        log.info("Finished executing ScheduledIngestionJob.");
    }

}
