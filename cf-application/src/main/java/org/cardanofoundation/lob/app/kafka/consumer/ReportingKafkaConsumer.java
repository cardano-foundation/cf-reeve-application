package org.cardanofoundation.lob.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.reporting.dto.events.ReportsLedgerUpdatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = {"lob.reporting.enabled", "spring.kafka.enabled"}, havingValue = "true", matchIfMissing = true)
public class ReportingKafkaConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "${lob.reporting.topics.reports-ledger-updated-event}")
    public void listen(ReportsLedgerUpdatedEvent message) {
        log.info("Received ReportsLedgerUpdatedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }
}
