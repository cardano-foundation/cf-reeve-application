package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.CoreTransactionsUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final OrganisationPublicApi organisationPublicApi;

    private final TransactionLineConverter transactionLineConverter;

    private final AccountingCoreRepository accountingCoreRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public List<String> findAllDispatchedCompletedAndFinalisedTxLineIds(String organisationId, List<String> importingTxLineIds) {
        return accountingCoreRepository.findDoneTxLineIds(organisationId, importingTxLineIds);
    }

    @Transactional(readOnly = true)
    public List<String> findAllTransactionLineIdsNotDispatchedYet(String organisationId, List<String> importingTxLineIds) {
        return accountingCoreRepository.findNotYetDispatchedAndFailedTxLineIds(organisationId, importingTxLineIds);
    }

    @Transactional
    public void updateDispatchStatusesForTransactionLines(Map<String, TransactionLine.LedgerDispatchStatus> statusMap) {
        log.info("Updating dispatch status for statusMapCount: {}", statusMap.size());

        for (val entry : statusMap.entrySet()) {
            val txLineId = entry.getKey();
            val status = entry.getValue();

            val txLineIdM = accountingCoreRepository.findById(txLineId);

            txLineIdM.ifPresent(txLine -> {
                txLine.setLedgerDispatchStatus(status);
                accountingCoreRepository.saveAndFlush(txLine);
            });
        }

        log.info("Updated dispatch status for statusMapCount: {} completed.", statusMap.size());
    }

    @Transactional(readOnly = true)
    public List<TransactionLine> readPendingTransactionLines(Organisation organisation) {
        // TODO what about order by entry date or transaction internal number, etc?
        val pendingTransactionLines = accountingCoreRepository
                .findByPendingTransactionLinesByOrganisationAndDispatchStatus(organisation.id(), List.of(NOT_DISPATCHED, FAILED));

        return pendingTransactionLines
                .stream()
                .map(transactionLineConverter::convert)
                .toList();
    }

    @Transactional
    public void storeAll(TransactionLines transactionLines) {
        //log.info("Storing transaction data: {}", transactionData);
        val entityTxLines = transactionLines.entries().stream()
                .map(transactionLineConverter::convert)
                .toList();

        List<String> updatedTxLineIds = accountingCoreRepository.saveAllAndFlush(entityTxLines)
                .stream().map(TransactionLineEntity::getId)
                .toList();

        log.info("Updated transaction line ids count: {}", updatedTxLineIds.size());

        applicationEventPublisher.publishEvent(new CoreTransactionsUpdatedEvent(transactionLines.organisationId(), updatedTxLineIds));
    }

    @Transactional
    public void publishLedgerEvents() {
        log.info("publishLedgerEvents...");

        for (val organisation : organisationPublicApi.listAll()) {
            val pendingTxLines = readPendingTransactionLines(organisation);
            log.info("Processing organisationId: {} - pendingTxLinesCount: {}", organisation.id(), pendingTxLines.size());

            log.info("Publishing PublishToTheLedgerEvent...");
            applicationEventPublisher.publishEvent(new LedgerUpdateCommand(organisation.id(), new TransactionLines(organisation.id(), pendingTxLines)));
        }
    }

    @Transactional
    public void scheduleIngestion() {
        log.info("Executing ScheduledIngestionJob...");

        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));
    }

}
