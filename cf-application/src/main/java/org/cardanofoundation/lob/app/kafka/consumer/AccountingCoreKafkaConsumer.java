package org.cardanofoundation.lob.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchStartedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.ReportsLedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TxsLedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationChunkEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFailedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationFinalisationEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationStartedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = {"lob.accounting_reporting_core.enabled", "spring.kafka.enabled"}, havingValue = "true", matchIfMissing = true)
public class AccountingCoreKafkaConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.tx-ledger-updated-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(TxsLedgerUpdatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.reports-ledger-updated-event}")
    public void listen(ReportsLedgerUpdatedEvent message) {
        log.info("Received ReportsLedgerUpdatedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.transaction-batch-failed-event}")
    public void listen(TransactionBatchFailedEvent message) {
        log.info("Received TransactionBatchFailedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.transaction-batch-started-event}")
    public void listen(TransactionBatchStartedEvent message) {
        log.info("Received TransactionBatchStartedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.transaction-batch-chunk-event}")
    public void listen(TransactionBatchChunkEvent message) {
        log.info("Received TransactionBatchChunkEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.reconcilation-failed-event}")
    public void listen(ReconcilationFailedEvent message) {
        log.info("Received ReconcilationFailedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.reconcilation-started-event}")
    public void listen(ReconcilationStartedEvent message) {
        log.info("Received ReconcilationStartedEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.reconcilation-chunk-event}")
    public void listen(ReconcilationChunkEvent message) {
        log.info("Received ReconcilationChunkEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.accounting_reporting_core.topics.reconcilation-finalisation-event}")
    public void listen(ReconcilationFinalisationEvent message) {
        log.info("Received ReconcilationFinalisationEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }


}
