package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
@ToString
public class Transaction {

    private String transactionNumber;

    @Builder.Default
    private List<TransactionLine> transactionLines = new ArrayList<>();


    public static List<Transaction> from(Map<String, List<TransactionLine>> transactions) {
        List<Transaction> result = new ArrayList<>();

        transactions.forEach((transactionNumber, transactionLines) -> {
            result.add(new Transaction(transactionNumber, transactionLines));
        });

        return result;
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
