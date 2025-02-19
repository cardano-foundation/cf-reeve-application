package org.cardanofoundation.lob.app.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.ReportLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand;
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

    @KafkaListener(topics = "accounting_reporting_core.domain.event.ledger.ReportLedgerUpdateCommand")
    public void listen(ReportLedgerUpdateCommand message) {
        log.info("Received LedgerUpdateCommand 123: {}", message);
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand")
    public void listen(TransactionLedgerUpdateCommand message) {
        log.info("Received LedgerUpdateCommand: {}", message);
        applicationEventPublisher.publishEvent(message);
    }
}
