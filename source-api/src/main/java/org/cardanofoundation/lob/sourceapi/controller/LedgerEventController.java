package org.cardanofoundation.lob.sourceapi.controller;


import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.lob.common.model.*;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationApprovalRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationApprovalResponse;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationRequest;
import org.cardanofoundation.lob.common.model.rest.LedgerEventRegistrationResponse;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRegistrationRepository;
import org.cardanofoundation.lob.sourceapi.repository.LedgerEventRepository;
import org.cardanofoundation.lob.sourceapi.repository.TxSubmitJobRepository;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/events")
@Log4j2
public class LedgerEventController {

    @Autowired
    private AmqpTemplate template;

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
            ledgerEventRepository.saveAll(ledgerEventRegistration.getLedgerEvents());
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

    @GetMapping("/tx/pending")
    public Flux<TxSubmitJob> getAllTransactionsPending() {
        return Flux.fromIterable(txSubmitJobRepository.findByJobStatus(TxSubmitJobStatus.PENDING));
    }

    @RequestMapping("/tx/resubmit/{id}")
    public String resubmit(@PathVariable(value="id") String id ){
        txSubmitJobRepository.findById(Integer.valueOf(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration has already been approved."));
        template.convertAndSend("txJobs", id);
        log.info("txJobs", id);
        return "done: " + id;
    }

    @PostMapping("/registrations/approve")
    public Mono<LedgerEventRegistrationApprovalResponse> approveRegistration(@RequestBody final LedgerEventRegistrationApprovalRequest approvalRequest) {
        try {
            //* Don't check that exist and the status */
            final LedgerEventRegistrationJob registrationJob = ledgerEventRegistrationRepository.findById(approvalRequest.getRegistrationId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration has already been approved."));
            registrationJob.setJobStatus(LedgerEventRegistrationJobStatus.APPROVED);
            ledgerEventRegistrationRepository.save(registrationJob);

            /**
             * @// TODO: 19/09/2023 Create message with the registrationJob id.
             */
            template.convertAndSend("myqueue", registrationJob.getRegistrationId());


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
