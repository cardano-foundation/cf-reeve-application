package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Transaction {

    @NotBlank
    private String id;

    @Size(min = 1, max =  255) @NotBlank String internalTransactionNumber;

    @NotNull
    private LocalDate entryDate;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private Organisation organisation;

    @NotNull
    @Builder.Default
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @NotNull
    @PositiveOrZero
    private BigDecimal fxRate;

    @NotNull
    @Builder.Default
    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;

    @Builder.Default
    private boolean transactionApproved = false;

    @Builder.Default
    private boolean ledgerDispatchApproved = false;

    @NotNull
    private YearMonth accountingPeriod;

    @Builder.Default
    @NotEmpty
    private Set<TransactionItem> items = new LinkedHashSet<>();

    public static String id(String organisationId,
                            String internalTransactionNumber) {
        return digestAsHex(STR."\{organisationId}::\{internalTransactionNumber}");
    }

}
