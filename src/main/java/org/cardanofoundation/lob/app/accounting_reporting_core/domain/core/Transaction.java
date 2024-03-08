package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsHex;

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

    @Builder.Default
    private Optional<Document> document = Optional.empty(); // initially we allow empty but later as part of business rules we check if document is present

    @NotNull
    @Builder.Default
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @NotNull
    @PositiveOrZero
    private BigDecimal fxRate;

    @Builder.Default
    private Optional<CostCenter> costCenter = Optional.empty();

    @Builder.Default
    private Optional<Project> project = Optional.empty();

    @NotNull
    @Builder.Default
    private ValidationStatus validationStatus = ValidationStatus.NOT_VALIDATED;

    @Builder.Default
    private boolean transactionApproved = false;

    @Builder.Default
    private boolean ledgerDispatchApproved = false;

    @Builder.Default
    @NotEmpty
    private Set<TransactionItem> transactionItems = new HashSet<>();

    public static String id(String organisationId,
                            String internalTransactionNumber) {
        return digestAsHex(STR."\{organisationId}::\{internalTransactionNumber}");
    }

}
