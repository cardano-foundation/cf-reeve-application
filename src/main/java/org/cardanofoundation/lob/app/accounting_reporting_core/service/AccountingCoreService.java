package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionData;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountingCoreService {

    private final AccountingCoreRepository accountingCoreRepository;

    @Transactional
    public void store(TransactionData transactionData) {
        log.info("Storing transaction data: {}", transactionData);
        val entityTxLines = transactionData.lines().stream().map(txLine -> {

            val entityTxLine = new org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionLine();

            entityTxLine.setId(txLine.id());
            entityTxLine.setOrganisationId(txLine.organisationId());
            entityTxLine.setTransactionType(txLine.transactionType());
            entityTxLine.setEntryDate(txLine.entryDate());
            entityTxLine.setTransactionNumber(txLine.transactionNumber());
            entityTxLine.setAccountCodeDebit(txLine.accountCodeDebit());

            entityTxLine.setBaseCurrency(txLine.baseCurrency().toUpperCase());
            entityTxLine.setCurrency(txLine.currency().toUpperCase());
            entityTxLine.setFxRate(txLine.fxRate());

            entityTxLine.setDocumentNumber(txLine.documentNumber().orElse(null));

            entityTxLine.setVendorCode(txLine.vendorCode().orElse(null));
            entityTxLine.setVendorName(txLine.vendorName().orElse(null));
            entityTxLine.setCostCenter(txLine.costCenter().orElse(null));
            entityTxLine.setProjectCode(txLine.projectCode().orElse(null));
            entityTxLine.setVatCode(txLine.vatCode().orElse(null));
            entityTxLine.setAccountNameDebit(txLine.accountNameDebit().orElse(null));
            entityTxLine.setAccountCredit(txLine.accountCredit().orElse(null));
            entityTxLine.setMemo(txLine.memo().orElse(null));

            entityTxLine.setAmountFcy(txLine.amountFcy().orElse(null));
            entityTxLine.setAmountLcy(txLine.amountLcy().orElse(null));


            return entityTxLine;
        }).toList();

        // TODO this is obviously wrong but for now we overwrite the previous values
        // but for now it's ok
        accountingCoreRepository.saveAllAndFlush(entityTxLines);
    }

}
