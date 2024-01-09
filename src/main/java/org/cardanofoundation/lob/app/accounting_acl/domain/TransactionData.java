package org.cardanofoundation.lob.app.accounting_acl.domain;

import java.util.List;

public record TransactionData(List<TransactionLine> lines) {
}
