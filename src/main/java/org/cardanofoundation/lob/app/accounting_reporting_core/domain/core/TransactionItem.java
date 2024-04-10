package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

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

    @NotBlank private String id;

    @NotNull private BigDecimal amountFcy;

    @NotNull private BigDecimal amountLcy;

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountCodeDebit = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountCodeEventRefDebit = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountNameDebit = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountCodeCredit = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountCodeEventRefCredit = Optional.empty();

    @Builder.Default
    private Optional<@Size(min = 1, max =  255) String> accountEventCode = Optional.empty();

    @Builder.Default
    private Optional<Project> project = Optional.empty();

    @Builder.Default
    private Optional<CostCenter> costCenter = Optional.empty();

    @Builder.Default
    private Optional<Document> document = Optional.empty(); // initially we allow empty but later as part of business rules we check if document is present

    public static String id(String transactionId,
                            String lineNo) {
        return digestAsHex(STR."\{transactionId}::\{lineNo}");
    }

    public boolean isTheSameBusinessWise(TransactionItem other) {
        val equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(this.id, other.id);
        equalsBuilder.append(this.amountFcy, other.amountFcy);
        equalsBuilder.append(this.amountLcy, other.amountLcy);
        equalsBuilder.append(this.accountCodeDebit, other.accountCodeDebit);
        equalsBuilder.append(this.accountCodeEventRefDebit, other.accountCodeEventRefDebit);
        equalsBuilder.append(this.accountNameDebit, other.accountNameDebit);
        equalsBuilder.append(this.accountCodeCredit, other.accountCodeCredit);
        equalsBuilder.append(this.accountCodeEventRefCredit, other.accountCodeEventRefCredit);
        equalsBuilder.append(this.accountEventCode, other.accountEventCode);

        equalsBuilder.append(this.costCenter, other.costCenter);
        equalsBuilder.append(this.document, other.document);

        return equalsBuilder.isEquals()
                && this.project.map(Project::isTheSameBusinessWise).orElse(true)
                && this.costCenter.map(CostCenter::isTheSameBusinessWise).orElse(true)
                && this.document.map(Document::isTheSameBusinessWise).orElse(true);
    }

}
