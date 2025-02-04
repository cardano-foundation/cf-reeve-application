package org.cardanofoundation.lob.app.kafka;

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
@ConditionalOnProperty(value = "lob.accounting_reporting_core.enabled", havingValue = "true")
public class AccountingCoreKafkaConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = "accounting_reporting_core.domain.event.ledger.TxsLedgerUpdateEvent")
    public void listen(TxsLedgerUpdatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.ledger.ReportsLedgerUpdatedEvent")
    public void listen(ReportsLedgerUpdatedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.extraction.TransactionBatchFailedEvent")
    public void listen(TransactionBatchFailedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.extraction.TransactionBatchStartedEvent")
    public void listen(TransactionBatchStartedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.extraction.TransactionBatchChunkEvent")
    public void listen(TransactionBatchChunkEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ReconcilationFailedEvent")
    public void listen(ReconcilationFailedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ReconcilationStartedEvent")
    public void listen(ReconcilationStartedEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ReconcilationChunkEvent")
    public void listen(ReconcilationChunkEvent message) {
        applicationEventPublisher.publishEvent(message);
    }

    @KafkaListener(topics = "accounting_reporting_core.domain.event.reconcilation.ReconcilationFinalisationEvent")
    public void listen(ReconcilationFinalisationEvent message) {
        applicationEventPublisher.publishEvent(message);
    }


}
