package org.cardanofoundation.lob.app.accounting_reporting_core.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class PIIDataFilteringService implements Function<TransactionLines, TransactionLines> {

    @Override
    public TransactionLines apply(TransactionLines transactionLines) {
        log.info("Filtering out transaction lines for organisation, org_id: {}", transactionLines.organisationId());

        val censoredTxLines = transactionLines
                .entries()
                .stream()
                .map(transactionLine -> {
                    return transactionLine
                            .toBuilder()
                            .accountCodeCredit(Optional.empty())
                            .accountNameDebit(Optional.empty())
                            .accountCodeCredit(Optional.empty())
                            .vendorName(Optional.empty())
                            .build();
                }).toList();

        return new TransactionLines(transactionLines.organisationId(), censoredTxLines);
    }

}
