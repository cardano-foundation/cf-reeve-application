package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.accounting_reporting_core.util.SHA3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
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
    private Document document;

    @NotNull
    @Builder.Default
    private LedgerDispatchStatus ledgerDispatchStatus = LedgerDispatchStatus.NOT_DISPATCHED;

    @NotNull
    @PositiveOrZero
    private BigDecimal fxRate;

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> costCenterInternalNumber = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> projectInternalNumber = Optional.empty();

    @NotNull
    private ValidationStatus validationStatus;

    @Builder.Default
    private boolean ledgerDispatchApproved = false;

    @Builder.Default
    @NotEmpty
    private Set<TransactionItem> transactionItems = new HashSet<>();

    // DO NOT DELETE - this is used to over-write the builder`
    @Slf4j
    public static class TransactionBuilder {
        public Transaction.TransactionBuilder validationStatus(ValidationStatus validationStatus) {
            if (this.validationStatus == null || validationStatus.ordinal() >= this.validationStatus.ordinal()) {
                this.validationStatus = validationStatus;
            } else {
                log.warn("Validation status is not increasing: {} -> {}", this.validationStatus, validationStatus);
            }

            return this;
        }

    }

    public static String id(String organisationId,
                            String internalTransactionNumber) {
        return SHA3.digestAsBase64(STR."\{organisationId}::\{internalTransactionNumber}");
    }


    public record WithPossibleViolations(Transaction transaction,
                                         Set<Violation> violations) {

        public static WithPossibleViolations create(Transaction transaction) {
            return new WithPossibleViolations(transaction, Set.of());
        }

        public static WithPossibleViolations create(Transaction transaction, Violation violation) {
            return new WithPossibleViolations(transaction, Set.of(violation));
        }

        public static WithPossibleViolations create(Transaction transaction, Set<Violation> violation) {
            return new WithPossibleViolations(transaction, violation);
        }

    }

}
