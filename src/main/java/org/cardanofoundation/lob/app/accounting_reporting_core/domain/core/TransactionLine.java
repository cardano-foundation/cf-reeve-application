package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@ToString
public class TransactionLine {

    // mandatory values

    @NotBlank String id;

    @Size(min = 1, max =  255) @NotBlank String organisationId;

    @NotNull
    TransactionType transactionType;

    @NotNull
    LocalDate entryDate;

    @Size(min = 1, max =  255) @NotBlank String internalTransactionNumber;

    @NotNull UUID ingestionId;

    // base currency specific value to the organisation
    @Size(min = 1, max =  255) @NotBlank String baseCurrencyInternalId;

    @Size(min = 1, max =  255) @NotBlank String baseCurrencyId;

    // target currency to which we convert
    @Size(min = 1, max =  255) @NotBlank String targetCurrencyInternalId;

    @NotNull BigDecimal fxRate;

    @NotNull LedgerDispatchStatus ledgerDispatchStatus;

    @NotNull ValidationStatus validationStatus;

    @NotNull BigDecimal amountFcy;

    @NotNull BigDecimal amountLcy;

    boolean ledgerDispatchApproved;

    /// optionals below
    Optional<@Size(min = 1, max =  255) String> accountCodeDebit;

    Optional<@Size(min = 1, max =  255) String> targetCurrencyId;

    Optional<@Size(min = 1, max =  255) String> internalDocumentNumber;

    Optional<@Size(min = 1, max =  255) String> internalVendorCode;

    Optional<@Size(min = 1, max =  255) String> vendorName;

    Optional<@Size(min = 1, max =  255) String> internalCostCenterCode;

    Optional<@Size(min = 1, max =  255) String> internalProjectCode;

    Optional<@Size(min = 1, max =  255) String> vatInternalCode;

    Optional<@PositiveOrZero BigDecimal> vatRate;

    Optional<@Size(min = 1, max =  255) String> accountNameDebit;

    Optional<@Size(min = 1, max =  255) String> accountCodeCredit;

    // TODO equality in business sense will not include in the future e.g. ingestion_id
    public boolean isBusinessEqual(TransactionLine transactionLine) {
        return this.equals(transactionLine);
    }

    public static Map<String, List<TransactionLine>> toTransactionsProjection(List<TransactionLine> transactionLines) {
        return transactionLines.stream()
                .collect(groupingBy(TransactionLine::getInternalTransactionNumber));
    }

    public record WithPossibleViolation(TransactionLine transactionLine,
                                        Set<Violation> violations) {

        public static WithPossibleViolation create(TransactionLine transactionLine) {
            return new WithPossibleViolation(transactionLine, Set.of());
        }

        public static WithPossibleViolation create(TransactionLine transactionLine, Set<Violation> violations) {
            return new WithPossibleViolation(transactionLine, violations);
        }

    }

    @Slf4j
    public static class TransactionLineBuilder {

        public TransactionLineBuilder validationStatus(ValidationStatus validationStatus) {
            if (this.validationStatus == null || validationStatus.ordinal() > this.validationStatus.ordinal()) {
                this.validationStatus = validationStatus;
            } else {
                log.warn("Validation status is not increasing: {} -> {}", this.validationStatus, validationStatus);
            }

            return this;
        }

    }

}
