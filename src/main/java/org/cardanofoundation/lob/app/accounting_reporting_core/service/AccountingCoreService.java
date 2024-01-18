package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.IngestionStoredEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.PublishToTheLedgerEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine.LedgerDispatchStatus.NOT_DISPATCHED;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final OrganisationPublicApi organisationPublicApi;

    private final AccountingCoreRepository accountingCoreRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void updateDispatchStatus(Map<String, TransactionLine.LedgerDispatchStatus> statusMap) {
        log.info("Updating dispatch status for statusMap: {}", statusMap);

        for (val entry : statusMap.entrySet()) {
            val txLineId = entry.getKey();
            val status = entry.getValue();

            val txLineIdM = accountingCoreRepository.findById(txLineId);

            txLineIdM.ifPresent(txLine -> {
                log.info("Updating dispatch status for txLineId: {}, status: {}", txLineId, status);
                txLine.setLedgerDispatchStatus(status);
                accountingCoreRepository.saveAndFlush(txLine);
            });
        }

        log.info("Updated dispatch status for statusMap: {}", statusMap);
    }

    @Transactional(readOnly = true)
    public List<TransactionLine> readPendingTransactionLines(Organisation organisation) {
        // TODO what about order by entry date or transaction internal number, etc?
        val pendingTransactionLines = accountingCoreRepository.findByPendingTransactionLinesByOrganisationAndDispatchStatus(organisation.id(), List.of(NOT_DISPATCHED, FAILED));

        return pendingTransactionLines.stream().map(entityTxLine -> {
            return new TransactionLine(
                    entityTxLine.getId(),
                    entityTxLine.getOrganisationId(),
                    entityTxLine.getTransactionType(),
                    entityTxLine.getEntryDate(),
                    entityTxLine.getTransactionInternalNumber(),
                    entityTxLine.getAccountCodeDebit(),
                    new TransactionLine.CurrencyPair(
                            organisationPublicApi.findOrganisationCurrencyByInternalId(entityTxLine.getBaseCurrency().getInternalCode()).orElseThrow(),
                            organisationPublicApi.findByCurrencyId(entityTxLine.getBaseCurrency().getId()).orElseThrow()
                    ),
                    new TransactionLine.CurrencyPair(
                            organisationPublicApi.findOrganisationCurrencyByInternalId(entityTxLine.getTargetCurrency().getInternalCode()).orElseThrow(),
                            organisationPublicApi.findByCurrencyId(entityTxLine.getTargetCurrency().getId()).orElseThrow()
                    ),
                    entityTxLine.getFxRate(),
                    entityTxLine.getLedgerDispatchStatus(),
                    Optional.ofNullable(entityTxLine.getDocumentInternalNumber()),
                    Optional.ofNullable(entityTxLine.getTransactionInternalNumber()),
                    Optional.ofNullable(entityTxLine.getVendorInternalCode()),
                    Optional.ofNullable(entityTxLine.getVendorName()),
                    Optional.ofNullable(entityTxLine.getCostCenterInternalCode()),
                    Optional.ofNullable(entityTxLine.getVat()).map(vat -> new TransactionLine.VatPair(vat.getInternalCode(), vat.getRate())),
                    Optional.ofNullable(entityTxLine.getAccountNameDebit()),
                    Optional.ofNullable(entityTxLine.getAccountCredit()),
                    Optional.ofNullable(entityTxLine.getMemo()),
                    Optional.ofNullable(entityTxLine.getAmountFcy()),
                    Optional.ofNullable(entityTxLine.getAmountLcy())
            );
        }).toList();
    }

    @Transactional
    public void storeAll(TransactionData transactionData) {
        //log.info("Storing transaction data: {}", transactionData);
        val entityTxLines = transactionData.transactionLines().stream().map(txLine -> {
                    val entityTxLine = new TransactionLineEntity();

                    entityTxLine.setId(txLine.id());
                    entityTxLine.setOrganisationId(txLine.organisationId());
                    entityTxLine.setTransactionType(txLine.transactionType());
                    entityTxLine.setEntryDate(txLine.entryDate());
                    entityTxLine.setTransactionInternalNumber(txLine.internalTransactionNumber());
                    entityTxLine.setAccountCodeDebit(txLine.accountCodeDebit());

                    entityTxLine.setBaseCurrency(new Currency(txLine.baseCurrency().currency().id(), txLine.baseCurrency().organisationCurrency().internalId()));
                    entityTxLine.setTargetCurrency(new Currency(txLine.targetCurrency().currency().id(), txLine.targetCurrency().organisationCurrency().internalId()));
                    entityTxLine.setFxRate(txLine.fxRate());

                    entityTxLine.setDocumentInternalNumber(txLine.internalDocumentNumber().orElse(null));

                    entityTxLine.setVendorInternalCode(txLine.internalVendorCode().orElse(null));
                    entityTxLine.setVendorName(txLine.vendorName().orElse(null));
                    entityTxLine.setCostCenterInternalCode(txLine.internalCostCenterCode().orElse(null));
                    entityTxLine.setProjectInternalCode(txLine.internalProjectCode().orElse(null));

                    entityTxLine.setVat(txLine.vat().map(vp -> new Vat(vp.vatCode(), vp.vatRate())).orElse(null));

                    entityTxLine.setAccountNameDebit(txLine.accountNameDebit().orElse(null));
                    entityTxLine.setAccountCredit(txLine.accountCredit().orElse(null));
                    entityTxLine.setMemo(txLine.memo().orElse(null));

                    entityTxLine.setAmountFcy(txLine.amountFcy().orElse(null));
                    entityTxLine.setAmountLcy(txLine.amountLcy().orElse(null));

                    entityTxLine.setLedgerDispatchStatus(NOT_DISPATCHED);

                    return entityTxLine;
                })
                .toList();

        List<String> updatedTxLineIds = accountingCoreRepository.saveAllAndFlush(entityTxLines)
                .stream().map(TransactionLineEntity::getId)
                .toList();

        log.info("Updated transaction line ids count: {}", updatedTxLineIds.size());

        applicationEventPublisher.publishEvent(new IngestionStoredEvent(updatedTxLineIds));
    }

    @Transactional
    public void publishLedgerEvents() {
        for (val org : organisationPublicApi.listAll()) {
            val pendingTxLines = readPendingTransactionLines(org);
            log.info("Processing organisationId: {} - pendingTxLinesCount: {}", org.id(), pendingTxLines.size());

            log.info("Publishing PublishToTheLedgerEvent...");
            applicationEventPublisher.publishEvent(new PublishToTheLedgerEvent(org.id(), new TransactionData(pendingTxLines)));
        }
    }

}
