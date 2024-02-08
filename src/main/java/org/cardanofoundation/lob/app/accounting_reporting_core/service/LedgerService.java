package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.LedgerDispatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final OrganisationPublicApi organisationPublicApi;

    private final AccountingCoreRepository accountingCoreRepository;

    private final TransactionLineConverter transactionLineConverter;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final PIIDataFilteringService piiDataFilteringService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void updateTransactionsWithNewLedgerDispatchStatusesString(String organisationId,
                                                                      Set<LedgerUpdatedEvent.TxStatusUpdate> txStatusUpdates) {
        log.info("Updating dispatch status for statusMapCount: {}", txStatusUpdates.size());

        for (val txStatusUpdate : txStatusUpdates) {
            val txId = txStatusUpdate.txId();
            val txLines = accountingCoreRepository.findByInternalTransactionNumber(organisationId, txId);

            for (val txLine : txLines) {
                txLine.setLedgerDispatchStatus(txStatusUpdate.status());
            }
        }

        log.info("Updated dispatch status for statusMapCount: {} completed.", txStatusUpdates.size());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void publishLedgerEvents() {
        log.info("publishLedgerEvents...");

        for (val organisation : organisationPublicApi.listAll()) {
            val pendingTxLines = readPendingTransactionLines(organisation);
            log.info("Processing organisationId: {} - pendingTxLinesCount: {}", organisation.id(), pendingTxLines.size());

            log.info("Censoring transaction lines...");

            val censoredTransactionLines = piiDataFilteringService.apply(new TransactionLines(organisation.id(), pendingTxLines));

            log.info("Publishing PublishToTheLedgerEvent...");

            applicationEventPublisher.publishEvent(LedgerUpdateCommand.create(censoredTransactionLines));
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private List<TransactionLine> readPendingTransactionLines(Organisation organisation) {
        // TODO what about order by entry date or transaction internal number, etc?
        return accountingCoreRepository
                .findLedgerDispatchPendingTransactionLines(
                        organisation.id(),
                        List.of(LedgerDispatchStatus.NOT_DISPATCHED),
                        List.of(ValidationStatus.VALIDATED)
                )
                .stream()
                .map(transactionLineConverter::convert)
                .toList();
    }

}
