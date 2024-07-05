package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.SystemExtractionParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.UserExtractionParameters;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ExtractionParametersFilteringService {

    public Set<Transaction> applyExtractionParameters(UserExtractionParameters userExtractionParameters,
                                                      SystemExtractionParameters systemExtractionParameters,
                                                      Set<Transaction> txs) {
        return txs.stream()
                .filter(tx -> {
                    val txAccountingPeriod = tx.getAccountingPeriod();

                    return txAccountingPeriod.equals(systemExtractionParameters.getAccountPeriodFrom()) || txAccountingPeriod.isAfter(systemExtractionParameters.getAccountPeriodFrom())
                            &&
                            (txAccountingPeriod.equals(systemExtractionParameters.getAccountPeriodTo()) || txAccountingPeriod.isBefore(systemExtractionParameters.getAccountPeriodTo()));
                })
                .filter(tx -> userExtractionParameters.getOrganisationId().equals(tx.getOrganisation().getId()))
                .filter(tx -> {
                    val from = userExtractionParameters.getFrom();

                    return tx.getEntryDate().isEqual(from) ||  tx.getEntryDate().isAfter(from);
                })
                .filter(tx -> {
                    val to = userExtractionParameters.getFrom();

                    return tx.getEntryDate().isEqual(to) ||  tx.getEntryDate().isAfter(to);
                })
                .filter(tx -> {
                    val txTypes = userExtractionParameters.getTransactionTypes();

                    return txTypes.isEmpty() || txTypes.contains(tx.getTransactionType());
                })
                .filter(tx -> {
                    val transactionNumber = userExtractionParameters.getTransactionNumbers();

                    return transactionNumber.isEmpty() || transactionNumber.contains(tx.getInternalTransactionNumber());
                })
                .collect(Collectors.toSet());
    }

}
