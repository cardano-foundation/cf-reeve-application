package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;


import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;

public interface BusinessRulesConvertor {

    TransformationResult runPreValidation(TransactionLines transactionLines, TransactionLines ignoredTransactionLines);

    TransformationResult runPostValidation(TransactionLines transactionLines, TransactionLines ignoredTransactionLines);

}
