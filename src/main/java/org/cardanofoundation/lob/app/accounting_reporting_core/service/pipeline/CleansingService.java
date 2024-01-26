package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.springframework.stereotype.Service;

@Service
public class CleansingService {

    public TransformationResult run(TransactionLines transactionLines) {
        return TransformationResult.create(transactionLines);
    }

}
