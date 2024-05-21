package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.FAIL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionStatus.SUCCESS;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

public class TransactionEntityListener {

    protected TransactionStatus calcStatus(TransactionEntity transactionEntity) {
        if (transactionEntity.allApprovalsPassedForTransactionDispatch()
                && transactionEntity.isRejectionFree()
                && transactionEntity.getAutomatedValidationStatus() == VALIDATED
        ) {
            return SUCCESS;
        }

        return FAIL;
    }

    @PrePersist
    @PreUpdate
    public void update(TransactionEntity transactionEntity) {
        transactionEntity.setStatus(calcStatus(transactionEntity));
    }

}
