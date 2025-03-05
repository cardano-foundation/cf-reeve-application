package org.cardanofoundation.lob.app.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchStartedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFinalisationEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationStartedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = {"lob.netsuite.enabled", "spring.kafka.enabled"}, havingValue = "true")
public class NetsuiteKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${lob.accounting_reporting_core.topics.reconcilation-failed-event}")
    private String reconcilationFailedEventTopic;
    @Value("${lob.accounting_reporting_core.topics.reconcilation-started-event}")
    private String reconcilationStartedEventTopic;
    @Value("${lob.accounting_reporting_core.topics.reconcilation-chunk-event}")
    private String reconcilationChunkEventTopic;
    @Value("${lob.accounting_reporting_core.topics.reconcilation-finalisation-event}")
    private String reconcilationFinalisationEventTopic;
    @Value("${lob.accounting_reporting_core.topics.transaction-batch-chunk-event}")
    private String transactionBatchChunkEventTopic;
    @Value("${lob.accounting_reporting_core.topics.transaction-batch-failed-event}")
    private String transactionBatchFailedEventTopic;
    @Value("${lob.accounting_reporting_core.topics.transaction-batch-started-event}")
    private String transactionBatchStartedEventTopic;

    @EventListener
    public void handleReconcilationFailedEvent(ReconcilationFailedEvent event) {
        log.info("Sending ReconcilationFailedEvent to Kafka: {}", event);
        kafkaTemplate.send(reconcilationFailedEventTopic, event);
    }

    @EventListener
    public void handleReconcilationStartedEvent(ReconcilationStartedEvent event) {
        log.info("Sending ReconcilationStartedEvent to Kafka: {}", event);
        kafkaTemplate.send(reconcilationStartedEventTopic, event);
    }

    @EventListener
    public void handleReconcilationChunkEvent(ReconcilationChunkEvent event) {
        log.info("Sending ReconcilationChunkEvent to Kafka: {}", event);
        kafkaTemplate.send(reconcilationChunkEventTopic, event);
    }

    @EventListener
    public void handleReconcilationFinalisationEvent(ReconcilationFinalisationEvent event) {
        log.info("Sending ReconcilationFinalisationEvent to Kafka: {}", event);
        kafkaTemplate.send(reconcilationFinalisationEventTopic, event);
    }

    @EventListener
    public void handleTransactionBatchChunkEvent(TransactionBatchChunkEvent event) {
        log.info("Sending TransactionBatchChunkEvent to Kafka: {}", event);
        kafkaTemplate.send(transactionBatchChunkEventTopic, event);
    }

    @EventListener
    public void handleTransactionBatchFailedEvent(TransactionBatchFailedEvent event) {
        log.info("Sending TransactionBatchFailedEvent to Kafka: {}", event);
        kafkaTemplate.send(transactionBatchFailedEventTopic, event);
    }

    @EventListener
    public void handleTransactionBatchStartedEvent(TransactionBatchStartedEvent event) {
        log.info("Sending TransactionBatchStartedEvent to Kafka: {}", event);
        kafkaTemplate.send(transactionBatchStartedEventTopic, event);
    }

}
