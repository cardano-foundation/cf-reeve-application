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

    public record WithPossibleViolation(Transaction transaction,
                                        Set<Violation> violations) {

        public static Transaction.WithPossibleViolation create(Transaction transaction) {
            return new Transaction.WithPossibleViolation(transaction, Set.of());
        }

        public static Transaction.WithPossibleViolation create(Transaction transaction, Violation violation) {
            return new Transaction.WithPossibleViolation(transaction, Set.of(violation));
        }

        public static Transaction.WithPossibleViolation create(Transaction transaction, Set<Violation> violation) {
            return new Transaction.WithPossibleViolation(transaction, violation);
        }

    }

}
