package org.cardanofoundation.lob.app.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ValidateIngestionEvent;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${lob.netsuite.topics.scheduled-ingestion-event}")
    public void listen(ScheduledIngestionEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.netsuite.topics.validate-ingestion-event}")
    public void listen(ValidateIngestionEvent message) {
        log.info("Received ValidateIngestionEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.netsuite.topics.transaction-batch-created-event}")
    public void listen(TransactionBatchCreatedEvent message) {
        log.info("Received TransactionBatchCreatedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.netsuite.topics.scheduled-reconcilation-event}")
    public void listen(ScheduledReconcilationEvent message) {
        log.info("Received ScheduledReconcilationEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.netsuite.topics.reconcilation-created-event}")
    public void listen(ReconcilationCreatedEvent message) {
        log.info("Received ReconcilationCreatedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

}
