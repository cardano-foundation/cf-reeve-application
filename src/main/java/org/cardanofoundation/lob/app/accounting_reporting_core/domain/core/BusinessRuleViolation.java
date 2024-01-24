package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

public record BusinessRuleViolation(String txLineId, String transactionNumber, String violation) {
}
