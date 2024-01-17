package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.event.IngestionStoredEvent;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final AccountingCoreRepository accountingCoreRepository;

    //private final BlockchainPublisherApi blockchainPublisherApi;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void store(TransactionData transactionData) {
        //log.info("Storing transaction data: {}", transactionData);

        val entityTxLines = transactionData.lines().stream().map(txLine -> {
            val entityTxLine = new org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLine();

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

            return entityTxLine;

            // currencyId empotency check
        })
//                .filter(txLine -> !blockchainPublisherApi.isPublished(txLine.getTransactionInternalNumber(), txLine.getId()))
        // we can update only those that have not been published yet
        .toList();

        // it's ok to overwrite previous values as long as it  has not been published to the blockchain

        var storedTransactionLineIds = accountingCoreRepository.saveAllAndFlush(entityTxLines)
                .stream()
                .map(TransactionLine::getId)
                .toList();

        log.info("Storing transaction line count: {}", storedTransactionLineIds.size());

        var onlyUpdated = transactionData.lines().stream().filter(txLine -> storedTransactionLineIds.contains(txLine.id())).toList();

        applicationEventPublisher.publishEvent(new IngestionStoredEvent(new TransactionData(onlyUpdated)));
    }

}
