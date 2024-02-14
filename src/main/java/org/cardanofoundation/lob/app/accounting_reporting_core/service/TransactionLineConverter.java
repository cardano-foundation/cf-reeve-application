package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("accounting_reporting_core.TransactionLineConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionLineConverter {

    public TransactionLineEntity convert(TransactionLine txLine) {
        val entityTxLine = new TransactionLineEntity();

        entityTxLine.setId(txLine.getId());
        entityTxLine.setOrganisation(Organisation.builder()
                .id(txLine.getOrganisationId())
                .currency(Currency.builder()
                        .id(txLine.getOrganisationCurrencyId())
                        .internalCode(txLine.getOrganisationCurrencyInternalId())
                        .build())
                .build());

        entityTxLine.setTransactionType(txLine.getTransactionType());
        entityTxLine.setEntryDate(txLine.getEntryDate());
        entityTxLine.setTransactionInternalNumber(txLine.getInternalTransactionNumber());
        entityTxLine.setAccountCodeDebit(txLine.getAccountCodeDebit().orElse(null));

        entityTxLine.setDocument(Document.builder()
                .internalNumber(txLine.getDocumentCurrencyInternalId())
                .currency(Currency.builder()
                        .id(txLine.getDocumentCurrencyId().orElse(null))
                        .internalCode(txLine.getDocumentCurrencyInternalId())
                        .build())
                .vat(txLine.getDocumentVatInternalCode().map(vatInternalCode -> Vat.builder()
                        .internalCode(vatInternalCode)
                        .rate(txLine.getDocumentVatRate().orElse(null))
                        .build()).orElse(null))
                .counterparty(txLine.getCounterpartyInternalNumber().map(counterPartyInternalCode -> {
                    return Counterparty.builder()
                            .internalCode(counterPartyInternalCode)
                            .name(txLine.getCounterpartyInternalName().orElse(null))
                            .build();
                }).orElse(null))
                .build());

        entityTxLine.setFxRate(txLine.getFxRate());

        entityTxLine.setCostCenterInternalCode(txLine.getCostCenterInternalCode().orElse(null));
        entityTxLine.setProjectInternalCode(txLine.getProjectInternalCode().orElse(null));

        entityTxLine.setAccountNameDebit(txLine.getAccountNameDebit().orElse(null));
        entityTxLine.setAccountCodeCredit(txLine.getAccountCodeCredit().orElse(null));

        entityTxLine.setValidationStatus(txLine.getValidationStatus());

        entityTxLine.setAmountFcy(txLine.getAmountFcy());
        entityTxLine.setAmountLcy(txLine.getAmountLcy());
        entityTxLine.setLedgerDispatchApproved(txLine.isLedgerDispatchApproved());
        entityTxLine.setCreatedAt(LocalDateTime.now());
        entityTxLine.setUpdatedAt(LocalDateTime.now());
        entityTxLine.setCreatedBy("system");
        entityTxLine.setUpdatedBy("system");

        return entityTxLine;
    }

    public TransactionLine convert(TransactionLineEntity entityTxLine) {
        return TransactionLine.builder()
                .id(entityTxLine.getId())
                .organisationId(entityTxLine.getOrganisation().getId())
                .transactionType(entityTxLine.getTransactionType())
                .entryDate(entityTxLine.getEntryDate())
                .internalTransactionNumber(entityTxLine.getTransactionInternalNumber())
                .organisationCurrencyInternalId(entityTxLine.getOrganisation().getCurrency().getInternalCode())
                .organisationCurrencyId(entityTxLine.getOrganisation().getCurrency().getId().orElse(null))
                .documentCurrencyInternalId(entityTxLine.getDocument().getCurrency().getInternalCode())
                .documentCurrencyId(entityTxLine.getDocument().getCurrency().getId())
                .documentInternalNumber(entityTxLine.getDocument().getInternalNumber())
                .fxRate(entityTxLine.getFxRate())
                .ledgerDispatchStatus(entityTxLine.getLedgerDispatchStatus())
                .validationStatus(entityTxLine.getValidationStatus())
                .amountFcy(entityTxLine.getAmountFcy())
                .amountLcy(entityTxLine.getAmountLcy())
                .ledgerDispatchApproved(entityTxLine.getLedgerDispatchApproved())
                .accountCodeDebit(entityTxLine.getAccountCodeDebit())
                .accountCodeCredit(entityTxLine.getAccountCodeCredit())
                .counterpartyInternalNumber(entityTxLine.getDocument().getCounterparty().map(Counterparty::getInternalCode))
                .counterpartyInternalName(entityTxLine.getDocument().getCounterparty().map(Counterparty::getName))
                .costCenterInternalCode(entityTxLine.getCostCenterInternalCode())
                .projectInternalCode(entityTxLine.getProjectInternalCode())
                .documentVatInternalCode(entityTxLine.getDocument().getVat().map(Vat::getInternalCode))
                .documentVatRate(entityTxLine.getDocument().getVat().flatMap(Vat::getRate))
                .accountNameDebit(entityTxLine.getAccountNameDebit())
                .accountCodeCredit(entityTxLine.getAccountCodeCredit())
                .build();
    }

}
