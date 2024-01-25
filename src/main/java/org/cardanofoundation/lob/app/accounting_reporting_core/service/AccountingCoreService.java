package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.BusinessRuleViolation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.CoreTransactionsUpdatedEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.ScheduledIngestionEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.BusinessRulesValidator;
import org.cardanofoundation.lob.app.notification_gateway.domain.event.NotificationEvent;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED;
import static org.cardanofoundation.lob.app.notification_gateway.domain.core.NotificationSeverity.ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final OrganisationPublicApi organisationPublicApi;

    private final TransactionLineConverter transactionLineConverter;

    private final AccountingCoreRepository accountingCoreRepository;

    private final BusinessRulesValidator businessRulesValidator;

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
                .findByPendingTransactionLinesByOrganisationAndDispatchStatus(organisation.id(), List.of(NOT_DISPATCHED));

        return pendingTransactionLines
                .stream()
                .map(transactionLineConverter::convert)
                .toList();
    }

    @Transactional
    // TODO find better business name for this
    public void startCoreIngestion(TransactionLines transactionLines) {
        //log.info("Storing transaction data: {}", transactionData);

        // load entities from db based on ids from event

        val organisationId = transactionLines.organisationId();

        val txLines = transactionLines
                .entries()
                .stream()
                .toList();

        val dispatchedTxLineIds = findAllDispatchedCompletedAndFinalisedTxLineIds(organisationId, txLines.stream().map(TransactionLine::id)
                .toList());

        log.info("dispatchedTxLineIdsCount: {}", dispatchedTxLineIds.size());

        // here are conflicting ones, the ones that have already been dispatched
        val dispatchedTxLines = txLines.stream()
                .filter(txLine -> dispatchedTxLineIds.contains(txLine.id()))
                .toList();

        log.info("dispatchedTxLineCount: {}", dispatchedTxLines.size());

        if (!dispatchedTxLines.isEmpty()) {
            log.warn("Failed to update some transaction lines, count: {}. They are already dispatched to the blockchain.", dispatchedTxLines.size());

            val dispatchedTxLineIdsAsString = dispatchedTxLines
                    .stream()
                    .map(TransactionLine::id)
                    .collect(joining(","));

            applicationEventPublisher.publishEvent(NotificationEvent.create(
                    ERROR,
                    "CANNOT_UPDATE_TX_LINES_ERROR",
                    "Not possible to update transactions that have already been dispatched to the blockchain.",
                    STR . "Unable to update tx line ids as they have been dispatched: \{dispatchedTxLineIdsAsString}")
            );
        }

        val notDispatchedTxLines = txLines.stream()
                .filter(txLine -> !dispatchedTxLineIds.contains(txLine.id()))
                .toList();

        log.info("notDispatchedTxLinesCont: {}", notDispatchedTxLines.size());

        if (!notDispatchedTxLines.isEmpty()) {
            log.info("Storing notDispatchedTxLines: {}", notDispatchedTxLines.size());

            val notDispatchedTxLinesLines = new TransactionLines(organisationId, notDispatchedTxLines);

            val violations = businessRulesValidator.validate(organisationId, notDispatchedTxLinesLines);
            val violationsByTxLineIdMap = violations.stream()
                    .collect(groupingBy(BusinessRuleViolation::txLineId));

            val violationsByTransactionNumberMap = violations
                    .stream()
                    .collect(groupingBy(BusinessRuleViolation::transactionNumber));

            for (val violation : violations) {
                log.warn("Business rule violation: {}", violation);

                applicationEventPublisher.publishEvent(NotificationEvent.create(
                        ERROR,
                        "BUSINESS_RULE_VIOLATION_ERROR",
                        "Business rule violation.",
                        STR . "Business rule violation: \{violation}")
                );
            }

            val txLineWithValidation = txLines.stream().map(txLine -> {
                val txLineId = txLine.id();

                val txLineViolations = violationsByTxLineIdMap.getOrDefault(txLineId, List.of());
                val txLineViolationsByTransactionNumber = violationsByTransactionNumberMap.getOrDefault(txLine.internalTransactionNumber(), List.of());
                val validated = txLineViolations.isEmpty() && txLineViolationsByTransactionNumber.isEmpty();

                return TransactionLine.recreateWithValidation(txLine, validated);
            }).toList();

            val entityTxLines = transactionLines.entries().stream()
                    .map(transactionLineConverter::convert)
                    .toList();

            List<String> updatedTxLineIds = accountingCoreRepository.saveAllAndFlush(entityTxLines)
                    .stream().map(TransactionLineEntity::getId)
                    .toList();

            log.info("Updated transaction line ids count: {}", updatedTxLineIds.size());

            applicationEventPublisher.publishEvent(new CoreTransactionsUpdatedEvent(transactionLines.organisationId(), updatedTxLineIds));
        }
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
        log.info("scheduleIngestion...");
        applicationEventPublisher.publishEvent(new ScheduledIngestionEvent("system"));
    }

}
