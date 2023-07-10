package org.cardanofoundation.lob.sourceapi.controller;


import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.model.*;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationApprovalRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRepository;
import org.cardanofoundation.lob.sourceapi.repository.TxSubmitJobRepository;
import org.cardanofoundation.lob.sourceapi.service.TxPackagingService;
import org.cardanofoundation.lob.sourceapi.service.TxSubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/events")
@Log4j2
public class LedgerEventController {

    @Autowired
    private TxSubmitService txSubmitService;

    @Autowired
    private TxPackagingService txPackagingService;

    @Autowired
    private LedgerEventRegistrationRepository ledgerEventRegistrationRepository;

    @Autowired
    private LedgerEventRepository ledgerEventRepository;

    @Autowired
    private TxSubmitJobRepository txSubmitJobRepository;

    @PostMapping("/registrations")
    public Mono<LedgerEventRegistrationRequest> addEventRegistration(@RequestBody final LedgerEventRegistrationRequest ledgerEventRegistration) {
        if (ledgerEventRegistrationRepository.existsById(ledgerEventRegistration.getRegistrationId())) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ledger event registration with this registration id already exists."));
        } else {
            ledgerEventRepository.saveAll(ledgerEventRegistration.getLedgerEvents());
            final LedgerEventRegistrationJob ledgerEventRegistrationJob = new LedgerEventRegistrationJob();
            ledgerEventRegistrationJob.setRegistrationId(ledgerEventRegistration.getRegistrationId());
            ledgerEventRegistrationJob.setLedgerEvents(ledgerEventRegistration.getLedgerEvents());
            ledgerEventRegistrationJob.setJobStatus(LedgerEventRegistrationJobStatus.PENDING_APPROVAL);
            ledgerEventRegistrationRepository.save(ledgerEventRegistrationJob);
        }

        return Mono.just(ledgerEventRegistration);
    }

    @GetMapping("/registrations/pending")
    public Flux<LedgerEventRegistrationJob> getPendingEventRegistrations() {
        return Flux.fromIterable(ledgerEventRegistrationRepository.findByJobStatus(LedgerEventRegistrationJobStatus.PENDING_APPROVAL));
    }

    @PostMapping("/registrations/approve")
    public Mono<LedgerEventRegistrationJob> approveRegistration(@RequestBody final LedgerEventRegistrationApprovalRequest approvalRequest) {
        try {
            final LedgerEventRegistrationJob registrationJob = ledgerEventRegistrationRepository.findById(approvalRequest.getRegistrationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration has already been approved."));
            registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.APPROVED);
            ledgerEventRegistrationRepository.save(registrationJob);

            final List<TxSubmitJob> txSubmitJobs = txPackagingService.createTxJobs(registrationJob);
            txSubmitJobRepository.saveAll(txSubmitJobs);

            registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.PROCESSED);
            ledgerEventRegistrationRepository.save(registrationJob);

            txSubmitJobs.forEach(txSubmitJob -> {
                try {
                    txSubmitService.processTxSubmitJob(txSubmitJob).ifPresentOrElse(
                            (txId) -> {
                                txSubmitJob.setTransactionId(txId);
                                txSubmitJob.setJobStatus(TxSubmitJobStatus.SUBMITTED);
                            },
                            () -> {
                                txSubmitJob.setJobStatus(TxSubmitJobStatus.FAILED);
                            }
                    );
                } catch (final Exception e) {
                    log.error(String.format("Could not submit a transaction job-id: %d", txSubmitJob.getId()));
                }
            });

            txSubmitJobRepository.saveAll(txSubmitJobs);

            return Mono.just(registrationJob);
        } catch (final ResponseStatusException e) {
            return Mono.error(e);
        }
    }
}
