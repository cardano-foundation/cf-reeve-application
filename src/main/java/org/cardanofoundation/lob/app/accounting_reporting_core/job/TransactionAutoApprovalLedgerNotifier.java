package org.cardanofoundation.lob.app.accounting_reporting_core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TEMPORARY HELPER JOB TO SIMULATE THE USER APPROVING TRANSACTIONS

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionAutoApprovalLedgerNotifier {

    private final OrganisationPublicApi organisationPublicApi;
    private final AccountingCoreService accountingCoreService;

    @Scheduled(fixedDelayString = "PT10S")
    public void execute() {
        log.info("Executing AutoApprovalLedgerNotifier job...");

        for (val organisation : organisationPublicApi.listAll()) {
            accountingCoreService.readBatchAndApproveTransactions(organisation.getId());
        }

        log.info("Finished executing AutoApprovalLedgerNotifier job.");
    }

}
