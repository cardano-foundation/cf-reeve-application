package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
@EqualsAndHashCode
public class Transaction {

    @NotBlank
    private String id;

    @Size(min = 1, max =  255) @NotBlank String internalTransactionNumber;

    @NotBlank
    private String batchId;

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

    public boolean allApprovalsPassedForTransactionDispatch() {
        return transactionApproved && ledgerDispatchApproved;
    }

    public boolean isTheSameBusinessWise(Transaction other) {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.id, other.id);
        equalsBuilder.append(this.entryDate, other.entryDate);
        equalsBuilder.append(this.transactionType, other.transactionType);
        equalsBuilder.append(this.organisation, other.organisation);
        equalsBuilder.append(this.fxRate, other.fxRate);
        equalsBuilder.append(this.accountingPeriod, other.accountingPeriod);
        equalsBuilder.append(this.internalTransactionNumber, other.internalTransactionNumber);
        equalsBuilder.append(this.validationStatus, other.validationStatus);

        // Compare items only if all other fields are equal
        if (equalsBuilder.isEquals()) {
            // Ensure both sets are of the same size
            if (this.items.size() != other.items.size()) {
                return false;
            }

            return this.items.stream()
                    .allMatch(thisItem -> other.items.stream()
                            .anyMatch(thisItem::isTheSameBusinessWise));
        }

        return false;
    }

}
