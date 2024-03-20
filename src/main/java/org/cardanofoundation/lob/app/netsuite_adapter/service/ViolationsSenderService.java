package org.cardanofoundation.lob.app.netsuite_adapter.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.netsuite_adapter.domain.core.TransactionsWithViolations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ViolationsSenderService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendViolation(TransactionsWithViolations transactionsWithViolations) {
//        log.info("Sending violation...");
//
//
//        for (val txWithViolation : transactionsWithViolations) {
//            applicationEventPublisher.publishEvent(new ViolationEvent(txWithViolation., txWithViolation.violation()));
//        }
//
    }

}
