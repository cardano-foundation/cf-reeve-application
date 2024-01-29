package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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

    String accountCodeDebit;

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

    Optional<String> accountCredit;

    ValidationStatus validationStatus;

    BigDecimal amountFcy;

    BigDecimal amountLcy;

    // TODO equality in business sense will not include in the future e.g. ingestion_id
    public boolean isBusinessEqual(TransactionLine transactionLine) {
        return this.equals(transactionLine);
    }

    public enum LedgerDispatchStatus {
        NOT_DISPATCHED, // not dispatched to blockchain(s) yet

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
                                        Optional<Violation> violation) {

        public static WithPossibleViolation create(TransactionLine transactionLine) {
            return new WithPossibleViolation(transactionLine, Optional.empty());
        }

        public static WithPossibleViolation create(TransactionLine transactionLine, Violation violation) {
            return new WithPossibleViolation(transactionLine, Optional.of(violation));
        }

        public static WithPossibleViolation create(TransactionLine transactionLine, Optional<Violation> violation) {
            return new WithPossibleViolation(transactionLine, violation);
        }

    }

}
