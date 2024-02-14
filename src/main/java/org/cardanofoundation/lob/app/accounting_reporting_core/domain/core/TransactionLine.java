package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Builder(toBuilder = true)
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

    @Size(min = 1, max =  255) @NotBlank String organisationCurrencyInternalId;

    @Size(min = 1, max =  255) @NotBlank String organisationCurrencyId;

    @Size(min = 1, max =  255) @NotBlank String documentCurrencyInternalId;

    @Size(min = 1, max =  255) @NotBlank String documentInternalNumber;

    @NotNull BigDecimal fxRate;

    @NotNull LedgerDispatchStatus ledgerDispatchStatus;

    @NotNull ValidationStatus validationStatus;

    @NotNull BigDecimal amountFcy;

    @NotNull BigDecimal amountLcy;

    boolean ledgerDispatchApproved;

    /// optionals below
    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountCodeDebit = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> documentCurrencyId = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> counterpartyInternalNumber = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> counterpartyInternalName = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> costCenterInternalCode = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> projectInternalCode = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> documentVatInternalCode = Optional.empty();

    @Builder.Default
    Optional<@PositiveOrZero BigDecimal> documentVatRate = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountNameDebit = Optional.empty();

    @Builder.Default
    Optional<@Size(min = 1, max =  255) String> accountCodeCredit = Optional.empty();

    public static Map<OrgTransactionNumber, List<TransactionLine>> toTransactionsProjection(List<TransactionLine> transactionLines) {
        return transactionLines.stream()
                .collect(groupingBy(t -> new OrgTransactionNumber(t.getOrganisationId(), t.getInternalTransactionNumber())));
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

    // DO NOT DELETE - this is used to over-write the builder`
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
