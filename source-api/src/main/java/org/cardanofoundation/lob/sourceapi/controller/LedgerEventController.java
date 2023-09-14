package org.cardanofoundation.lob.sourceapi.controller;


import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.crypto.Hashing;
import org.cardanofoundation.lob.common.model.*;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationApprovalRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationApprovalResponse;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationResponse;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRepository;
import org.cardanofoundation.lob.sourceapi.repository.TxSubmitJobRepository;

import org.cardanofoundation.lob.txsubmitter.service.ServiceTxPackaging;
import org.cardanofoundation.lob.txsubmitter.service.ServiceTxSubmitter;
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
    private ServiceTxSubmitter serviceTxSubmit;

    @Autowired
    private ServiceTxPackaging serviceTxPackaging;

    @Autowired
    private LedgerEventRegistrationRepository ledgerEventRegistrationRepository;

    @Autowired
    private LedgerEventRepository ledgerEventRepository;

    @Autowired
    private TxSubmitJobRepository txSubmitJobRepository;

    @PostMapping("/registrations")
    public Mono<LedgerEventRegistrationResponse> addEventRegistration(@RequestBody final LedgerEventRegistrationRequest ledgerEventRegistration) {
        if (ledgerEventRegistrationRepository.existsById(ledgerEventRegistration.getRegistrationId())) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ledger event registration with this registration id already exists."));
        } else {
            /**
             * @// TODO: 11/09/2023 The saveAll create horphan data in th ledger_event table.
             */
            //ledgerEventRepository.saveAll(ledgerEventRegistration.getLedgerEvents());
            final LedgerEventRegistrationJob ledgerEventRegistrationJob = new LedgerEventRegistrationJob();
            ledgerEventRegistrationJob.setRegistrationId(ledgerEventRegistration.getRegistrationId());
            ledgerEventRegistrationJob.setLedgerEvents(ledgerEventRegistration.getLedgerEvents());
            ledgerEventRegistrationJob.setJobStatus(LedgerEventRegistrationJobStatus.PENDING_APPROVAL);
            try {
                ledgerEventRegistrationRepository.save(ledgerEventRegistrationJob);
            }catch (Exception e){
                log.error(e.getMessage());
            }


            final LedgerEventRegistrationResponse response = new LedgerEventRegistrationResponse();
            response.setRegistrationId(ledgerEventRegistration.getRegistrationId());
            response.setJobStatus(ledgerEventRegistrationJob.getJobStatus());
            return Mono.just(response);
        }
    }

    @GetMapping("/registrations/pending")
    public Flux<LedgerEventRegistrationJob> getPendingEventRegistrations() {
        return Flux.fromIterable(ledgerEventRegistrationRepository.findByJobStatus(LedgerEventRegistrationJobStatus.PENDING_APPROVAL));
    }

    @GetMapping("/registrations/all")
    public Flux<LedgerEventRegistrationJob> getAllEventRegistrations() {
        return Flux.fromIterable(ledgerEventRegistrationRepository.findAll());
    }

    @GetMapping("/tx/all")
    public Flux<TxSubmitJob> getAllTransactions() {
        return Flux.fromIterable(txSubmitJobRepository.findAll());
    }

    @PostMapping("/registrations/approve")
    public Mono<LedgerEventRegistrationApprovalResponse> approveRegistration(@RequestBody final LedgerEventRegistrationApprovalRequest approvalRequest) {
        try {
            //* Don't check that exist and the status */
            final LedgerEventRegistrationJob registrationJob = ledgerEventRegistrationRepository.findById(approvalRequest.getRegistrationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration has already been approved."));
            registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.APPROVED);
            ledgerEventRegistrationRepository.save(registrationJob);

            final List<TxSubmitJob> txSubmitJobs = serviceTxPackaging.createTxJobs(registrationJob);
            txSubmitJobRepository.saveAll(txSubmitJobs);

            registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.PROCESSED);
            ledgerEventRegistrationRepository.save(registrationJob);

            for (final TxSubmitJob txSubmitJob : txSubmitJobs) {
                try {
                    log.info("Processing: " + txSubmitJob.getTransactionMetadata().length + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));
                    serviceTxSubmit.processTxSubmitJob(txSubmitJob, Math.random()).ifPresentOrElse(
                            (txId) -> {
                                log.info("tx Id is: " + txId + " -- " + Hashing.blake2b256Hex(txSubmitJob.getTransactionMetadata()));
                                txSubmitJob.setTransactionId(txId);
                                txSubmitJob.setJobStatus(TxSubmitJobStatus.SUBMITTED);
                                log.info("Submit");
                            },
                            () -> {
                                txSubmitJob.setJobStatus(TxSubmitJobStatus.FAILED);
                                log.info("fail");
                            }
                    );
                } catch (final Exception e) {
                    log.error(String.format("Could not submit a transaction job-id: %d", txSubmitJob.getId()));
                    log.error(String.format("Could not submit a transaction error", e.getMessage().toString()));
                }

                txSubmitJobRepository.save(txSubmitJob);
            }

            txSubmitJobRepository.saveAll(txSubmitJobs);

            final LedgerEventRegistrationApprovalResponse response = new LedgerEventRegistrationApprovalResponse();
            response.setRegistrationId(approvalRequest.getRegistrationId());
            response.setJobStatus(registrationJob.getJobStatus());
            return Mono.just(response);
        } catch (final ResponseStatusException e) {
            log.error(e);
            return Mono.error(e);
        }
    }
}
