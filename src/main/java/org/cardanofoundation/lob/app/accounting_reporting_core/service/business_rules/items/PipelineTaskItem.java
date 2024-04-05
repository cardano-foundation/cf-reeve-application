package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;

public interface PipelineTaskItem {

    Transaction run(Transaction transaction);

}
