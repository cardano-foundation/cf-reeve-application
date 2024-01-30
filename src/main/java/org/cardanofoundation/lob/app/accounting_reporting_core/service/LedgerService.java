package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.LedgerUpdateCommand;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final OrganisationPublicApi organisationPublicApi;

    private final AccountingCoreRepository accountingCoreRepository;

    private final TransactionLineConverter transactionLineConverter;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void updateTransactionLines(Map<String, TransactionLine.LedgerDispatchStatus> statusMap) {
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

    @Transactional(readOnly = true)
    private List<TransactionLine> readPendingTransactionLines(Organisation organisation) {
        // TODO what about order by entry date or transaction internal number, etc?
        return accountingCoreRepository
                .findLedgerDispatchPendingTransactionLines(
                        organisation.id(),
                        List.of(TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED),
                        List.of(ValidationStatus.VALIDATED)
                )
                .stream()
                .map(transactionLineConverter::convert)
                .toList();
    }

}
