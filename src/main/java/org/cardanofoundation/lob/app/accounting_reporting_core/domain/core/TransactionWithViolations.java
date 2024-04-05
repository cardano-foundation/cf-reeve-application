//package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;
//
//import java.util.Set;
//
//public record TransactionWithViolations(Transaction transaction,
//                                     Set<Violation> violations) {
//
//    public static TransactionWithViolations create(Transaction transaction) {
//        return new TransactionWithViolations(transaction, Set.of());
//    }
//
//    public static TransactionWithViolations create(Transaction transaction, Violation violation) {
//        return new TransactionWithViolations(transaction, Set.of(violation));
//    }
//
//    public static TransactionWithViolations create(Transaction transaction, Set<Violation> violation) {
//        return new TransactionWithViolations(transaction, violation);
//    }
//
//}
