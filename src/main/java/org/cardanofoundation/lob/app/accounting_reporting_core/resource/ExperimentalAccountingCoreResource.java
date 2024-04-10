package org.cardanofoundation.lob.app.accounting_reporting_core.resource;


import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReIngestionIntents;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.internal.AccountingCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ReIngestionIntents.ReprocessType.ONLY_FAILED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/core")
@Slf4j
@RequiredArgsConstructor
public class ExperimentalAccountingCoreResource {

    private final AccountingCoreService accountingCoreService;

    @PostConstruct
    public void init() {
        log.info("AccountingCoreResource init.");
    }

    @RequestMapping(value = "/schedule/new", method = POST, produces = "application/json")
    public ResponseEntity<?> schedule() {
        val userExtractionParameters = UserExtractionParameters.builder()
                .from(LocalDate.now().minusYears(20))
                .to(LocalDate.now())
                .organisationId("75f95560c1d883ee7628993da5adf725a5d97a13929fd4f477be0faf5020ca94")
                //.transactionTypes(List.of(TransactionType.CardCharge, TransactionType.FxRevaluation))
                //.transactionNumbers(List.of("JOURNAL226", "JOURNAL227"))
                .build();

        accountingCoreService.scheduleIngestion(userExtractionParameters);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/reschedule/{batch_id}", method = POST, produces = "application/json")
    public ResponseEntity<?> reschedule(@Valid @PathVariable("batch_id") String batchId, @RequestParam("reprocess_type") Optional<ReIngestionIntents.ReprocessType> reprocessType) {
        return accountingCoreService.scheduleReIngestion(batchId, ReIngestionIntents.newIngestion(reprocessType.orElse(ONLY_FAILED)))
                .fold(problem -> {
                    return ResponseEntity.status(problem.getStatus().getStatusCode()).body(problem);
                }, success -> {
                    return ResponseEntity.ok().build();
                });
    }

}
