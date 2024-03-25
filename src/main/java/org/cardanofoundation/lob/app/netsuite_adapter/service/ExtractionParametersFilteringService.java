package org.cardanofoundation.lob.app.netsuite_adapter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.FilteringParameters;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExtractionParametersFilteringService {

    private final Clock clock;

    public Set<Transaction> applyExtractionParameters(FilteringParameters filteringParameters,
                                                      Organisation organisation,
                                                      Set<Transaction> txs) {
        return txs.stream()
                .filter(tx -> {
                    val currentAccountingPeriod = YearMonth.now(clock);

                    val start = currentAccountingPeriod.minusMonths(organisation.getAccountPeriodMonths());

                    val txAccountingPeriod = tx.getAccountingPeriod();

                    return txAccountingPeriod.equals(currentAccountingPeriod) || (txAccountingPeriod.isAfter(start) && txAccountingPeriod.isBefore(currentAccountingPeriod));
                })
                .filter(tx -> filteringParameters.getOrganisationId().equals(tx.getOrganisation().getId()))
                .filter(tx -> {
                    val from = filteringParameters.getFrom();

                    return tx.getEntryDate().isEqual(from) ||  tx.getEntryDate().isAfter(from);
                })
                .filter(tx -> {
                    val to = filteringParameters.getFrom();

                    return tx.getEntryDate().isEqual(to) ||  tx.getEntryDate().isAfter(to);
                })
                .filter(tx -> {
                    val txTypes = filteringParameters.getTransactionTypes();

                    return txTypes.isEmpty() || txTypes.contains(tx.getTransactionType());
                })
                .filter(tx -> {
                    val transactionNumber = filteringParameters.getTransactionNumbers();

                    return transactionNumber.isEmpty() || transactionNumber.contains(tx.getInternalTransactionNumber());
                })
                .collect(Collectors.toSet());
    }

}
