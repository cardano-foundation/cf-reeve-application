package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionWithViolations;

public interface PipelineTaskItem {

    TransactionWithViolations run(TransactionWithViolations transactionWithViolations);

}
