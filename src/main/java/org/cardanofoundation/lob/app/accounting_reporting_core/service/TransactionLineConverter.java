package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLineEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.Vat;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("accounting_reporting_core.TransactionLineConverter")
@Slf4j
@RequiredArgsConstructor
public class TransactionLineConverter {

    private final OrganisationPublicApi organisationPublicApi;

    public TransactionLineEntity convert(TransactionLine txLine) {
        val entityTxLine = new TransactionLineEntity();

        entityTxLine.setId(txLine.id());
        entityTxLine.setOrganisationId(txLine.organisationId());
        entityTxLine.setTransactionType(txLine.transactionType());
        entityTxLine.setEntryDate(txLine.entryDate());
        entityTxLine.setTransactionInternalNumber(txLine.internalTransactionNumber());
        entityTxLine.setAccountCodeDebit(txLine.accountCodeDebit());
        entityTxLine.setIngestionID(txLine.ingestionId());

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

        entityTxLine.setValidated(txLine.validated().orElse(null));

        entityTxLine.setAmountFcy(txLine.amountFcy().orElse(null));
        entityTxLine.setAmountLcy(txLine.amountLcy().orElse(null));

        return entityTxLine;
    }

    public TransactionLine convert(TransactionLineEntity entityTxLine) {
        return new TransactionLine(
                entityTxLine.getId(),
                entityTxLine.getOrganisationId(),
                entityTxLine.getTransactionType(),
                entityTxLine.getEntryDate(),
                entityTxLine.getTransactionInternalNumber(),
                entityTxLine.getIngestionID(),
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
                Optional.ofNullable(entityTxLine.getValidated()),
                Optional.ofNullable(entityTxLine.getAmountFcy()),
                Optional.ofNullable(entityTxLine.getAmountLcy())
        );
    }

}
