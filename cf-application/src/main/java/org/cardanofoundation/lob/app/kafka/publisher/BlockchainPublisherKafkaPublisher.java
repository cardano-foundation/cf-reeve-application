package org.cardanofoundation.lob.app.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.ReportsLedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TxsLedgerUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = {"lob.blockchain_publisher.enabled", "spring.kafka.enabled"}, havingValue = "true", matchIfMissing = false)
public class BlockchainPublisherKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${lob.accounting_reporting_core.topics.tx-ledger-updated-event}")
    private String txLedgerUpdatedEventTopic;
    @Value("${lob.accounting_reporting_core.topics.reports-ledger-updated-event}")
    private String reportsLedgerUpdatedEventTopic;

    @EventListener
    public void handleTxLedgerUpdatedEvent(TxsLedgerUpdatedEvent event) {
        log.info("Sending TxsLedgerUpdateEvent to Kafka: {}", event);
        kafkaTemplate.send(txLedgerUpdatedEventTopic, event);
    }

    @EventListener
    public void handleReportsLedgerUpdatedEvent(ReportsLedgerUpdatedEvent event) {
        log.info("Sending ReportsLedgerUpdatedEvent to Kafka: {}", event);
        kafkaTemplate.send(reportsLedgerUpdatedEventTopic, event);
    }
}
