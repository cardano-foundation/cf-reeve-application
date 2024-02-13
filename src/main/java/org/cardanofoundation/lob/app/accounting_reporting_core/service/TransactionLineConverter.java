package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service("accounting_reporting_core.TransactionLineConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionLineConverter {

    public TransactionLineEntity convert(TransactionLine txLine) {
        val entityTxLine = new TransactionLineEntity();

        entityTxLine.setId(txLine.getId());
        entityTxLine.setOrganisationId(txLine.getOrganisationId());
        entityTxLine.setTransactionType(txLine.getTransactionType());
        entityTxLine.setEntryDate(txLine.getEntryDate());
        entityTxLine.setTransactionInternalNumber(txLine.getInternalTransactionNumber());
        entityTxLine.setAccountCodeDebit(txLine.getAccountCodeDebit().orElse(null));

        entityTxLine.setBaseCurrencyInternalCode(txLine.getBaseCurrencyInternalId());
        entityTxLine.setBaseCurrencyId(txLine.getBaseCurrencyId());

        entityTxLine.setTargetCurrencyInternalCode(txLine.getTargetCurrencyInternalId());
        entityTxLine.setTargetCurrencyId(txLine.getTargetCurrencyId().orElse(null));

        entityTxLine.setFxRate(txLine.getFxRate());

        entityTxLine.setDocumentInternalNumber(txLine.getInternalDocumentNumber().orElse(null));

        entityTxLine.setCounterpartyInternalCode(txLine.getInternalCounterpartyCode().orElse(null));
        entityTxLine.setCounterpartyName(txLine.getInternalCounterpartyName().orElse(null));
        entityTxLine.setCostCenterInternalCode(txLine.getInternalCostCenterCode().orElse(null));
        entityTxLine.setProjectInternalCode(txLine.getInternalProjectCode().orElse(null));

        entityTxLine.setVatInternalCode(txLine.getVatInternalCode().orElse(null));
        entityTxLine.setVatRate(txLine.getVatRate().orElse(null));

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
        return new TransactionLine(
                entityTxLine.getId(),
                entityTxLine.getOrganisationId(),
                entityTxLine.getTransactionType(),
                entityTxLine.getEntryDate(),
                entityTxLine.getTransactionInternalNumber(),
                entityTxLine.getBaseCurrencyInternalCode(),
                entityTxLine.getBaseCurrencyId(),
                entityTxLine.getTargetCurrencyInternalCode(),
                entityTxLine.getFxRate(),
                entityTxLine.getLedgerDispatchStatus(),
                entityTxLine.getValidationStatus(),
                entityTxLine.getAmountFcy(),
                entityTxLine.getAmountLcy(),
                entityTxLine.getLedgerDispatchApproved(),
                Optional.ofNullable(entityTxLine.getAccountCodeDebit()),
                Optional.ofNullable(entityTxLine.getTargetCurrencyId()),
                Optional.ofNullable(entityTxLine.getDocumentInternalNumber()),
                Optional.ofNullable(entityTxLine.getCounterpartyInternalCode()),

                Optional.ofNullable(entityTxLine.getCounterpartyName()),
                Optional.ofNullable(entityTxLine.getCostCenterInternalCode()),
                Optional.ofNullable(entityTxLine.getProjectInternalCode()),

                Optional.ofNullable(entityTxLine.getVatInternalCode()),
                Optional.ofNullable(entityTxLine.getVatRate()),

                Optional.ofNullable(entityTxLine.getAccountNameDebit()),
                Optional.ofNullable(entityTxLine.getAccountCodeCredit())
        );
    }

}
