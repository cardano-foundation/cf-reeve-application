package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;

import java.util.Map;

public record LedgerChangeEvent(Map<String, TransactionLine.LedgerDispatchStatus> statusUpdatesMap) {

}
