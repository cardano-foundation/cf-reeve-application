package org.cardanofoundation.lob.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ScheduledReconcilationEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = {"lob.netsuite.enabled", "spring.kafka.enabled"}, havingValue = "true", matchIfMissing = true)
public class NetSuiteKafkaConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent")
    public void listen(ScheduledIngestionEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.extraction.TransactionBatchCreatedEvent")
    public void listen(TransactionBatchCreatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ScheduledReconcilationEvent")
    public void listen(ScheduledReconcilationEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent")
    public void listen(ReconcilationCreatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

}
