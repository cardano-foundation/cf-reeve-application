package org.cardanofoundation.lob.app.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.TransactionBatchCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.extraction.ValidateIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionLedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TransactionStatusRequestEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ledger.TxRollbackEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ReconcilationCreatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.reconcilation.ScheduledReconcilationEvent;
import org.cardanofoundation.lob.app.funding.domain.events.SpendingEventsPublishCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = {"lob.funding.enabled", "spring.kafka.enabled"}, havingValue = "true")
public class FundingPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    // Netsuite Topics
    @Value("${lob.funding.topics.spending-events-publish-command}")
    private String spendingEventsPublishCommandTopic;

    @EventListener
    public void handleSpendingSchedulePublishCommand(SpendingEventsPublishCommand event) {
        log.info("Sending SpendingEventsPublishCommand to Kafka: {}", event);
        kafkaTemplate.send(spendingEventsPublishCommandTopic, event);
    }

}
