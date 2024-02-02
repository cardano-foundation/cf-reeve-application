package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

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

    String id;

    String organisationId;

    TransactionType transactionType;

    LocalDate entryDate;

    String internalTransactionNumber;

    UUID ingestionId;

    Optional<String> accountCodeDebit;

    // base currency specific value to the organisation
    String baseCurrencyInternalId;

    String baseCurrencyId;

    // target currency to which we convert
    String targetCurrencyInternalId;

    Optional<String> targetCurrencyId;

    BigDecimal fxRate;

    LedgerDispatchStatus ledgerDispatchStatus;

    /// optionals below

    Optional<String> internalDocumentNumber;

    Optional<String> internalVendorCode;

    Optional<String> vendorName;

    Optional<String> internalCostCenterCode;

    Optional<String> internalProjectCode;

    Optional<String> vatInternalCode;

    Optional<BigDecimal> vatRate;

    Optional<String> accountNameDebit;

    Optional<String> accountCodeCredit;

    ValidationStatus validationStatus;

    BigDecimal amountFcy;

    BigDecimal amountLcy;

    boolean ledgerDispatchApproved;

    // TODO equality in business sense will not include in the future e.g. ingestion_id
    public boolean isBusinessEqual(TransactionLine transactionLine) {
        return this.equals(transactionLine);
    }

    public static Map<String, List<TransactionLine>> toTransactionsProjection(List<TransactionLine> transactionLines) {
        return transactionLines.stream()
                .collect(groupingBy(TransactionLine::getInternalTransactionNumber));
    }

    public enum LedgerDispatchStatus {
        NOT_DISPATCHED, // not dispatched to blockchain(s) yet

        STORED, // acking that we stored in the database

        DISPATCHED, // dispatched to blockchain(s)

        COMPLETED,

        FINALIZED; // finalised on blockchain(s)

        /**
         * Dispatchable means that we can dispatch the transaction line to the blockchain(s)
         */
        public boolean isDispatchable() {
            return this == NOT_DISPATCHED;
        }

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
