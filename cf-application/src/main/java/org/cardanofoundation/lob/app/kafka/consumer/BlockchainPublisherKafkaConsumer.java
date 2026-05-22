package org.cardanofoundation.lob.app.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionStatusRequestEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TxRollbackEvent;
import org.cardanofoundation.lob.app.reporting.dto.events.PublishReportEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = {"lob.blockchain_publisher.enabled", "spring.kafka.enabled"}, havingValue = "true", matchIfMissing = true)
public class BlockchainPublisherKafkaConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "${lob.blockchain_publisher.topics.transaction-ledger-update-commander}", groupId = "${lob.blockchain_publisher.consumer-group}")
    public void listen(TransactionLedgerUpdateCommand message) {
        log.info("Received LedgerUpdateCommand from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.reporting.topics.publish-report-event}", groupId = "${lob.blockchain_publisher.consumer-group}")
    public void listen(PublishReportEvent message) {
        log.info("Received PublishReportEvent from Kafa: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.blockchain_publisher.topics.tx-rollback-event}", groupId = "${lob.blockchain_publisher.consumer-group}")
    public void listen(TxRollbackEvent message) {
        log.info("Received TxRollbackEvent from Kafa: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "${lob.blockchain_publisher.topics.transaction-status-request-event}", groupId = "${lob.blockchain_publisher.consumer-group}")
    public void listen(TransactionStatusRequestEvent message) {
        log.info("Received TransactionStatusRequestEvent from Kafka: {}", message);
        applicationEventPublisher.publishEvent(message);
    }


}
