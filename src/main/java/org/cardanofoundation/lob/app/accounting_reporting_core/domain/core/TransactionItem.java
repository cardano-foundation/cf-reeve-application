package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.annotations.LOB_ERPVersionRelevant;

import java.math.BigDecimal;
import java.util.Optional;

import static org.cardanofoundation.lob.app.support.crypto.SHA3.digestAsHex;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransactionItem {

    @LOB_ERPVersionRelevant
    @NotBlank private String id;

    @LOB_ERPVersionRelevant
    @NotNull private BigDecimal amountFcy;

    @LOB_ERPVersionRelevant
    @NotNull private BigDecimal amountLcy;

    @Builder.Default
    @LOB_ERPVersionRelevant
    private Optional<Account> accountDebit = Optional.empty();

    @Builder.Default
    @LOB_ERPVersionRelevant
    private Optional<Account> accountCredit = Optional.empty();

    @Builder.Default
    private Optional<AccountEvent> accountEvent = Optional.empty();

    @Builder.Default
    @LOB_ERPVersionRelevant
    private Optional<Project> project = Optional.empty();

    @Builder.Default
    @LOB_ERPVersionRelevant
    private Optional<CostCenter> costCenter = Optional.empty();

    @Builder.Default
    @LOB_ERPVersionRelevant
    private Optional<Document> document = Optional.empty(); // initially we allow empty but later as part of business rules we check if document is present

    @NotNull
    @PositiveOrZero
    @LOB_ERPVersionRelevant
    private BigDecimal fxRate;

    public static String id(String transactionId,
                            String lineNo) {
        return digestAsHex(STR."\{transactionId}::\{lineNo}");
    }

}
