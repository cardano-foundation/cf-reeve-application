package org.cardanofoundation.lob.app.accounting_reporting_core.domain.event;

import java.util.List;

public record LedgerStoredEvent(List<String> txLinesId) {
}
