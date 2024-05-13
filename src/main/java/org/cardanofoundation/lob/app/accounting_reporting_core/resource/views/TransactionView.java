package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionItemEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Change the Transaction class name.
 */
@Getter
@Setter
@AllArgsConstructor
public class TransactionView {

    private String id;
    private String internalTransactionNumber;
    private LocalDate entryDate;
    private TransactionType transactionType;
    private ValidationStatus validationStatus = ValidationStatus.VALIDATED;
    private boolean transactionApproved = false;
    private boolean ledgerDispatchApproved = false;
    private Set<TransactionItemView> items = new LinkedHashSet<>();
    private Set<ViolationView> violations = new LinkedHashSet<>();

    public String getEntryDate() {
        return entryDate.format(DateTimeFormatter.ofPattern("y-M-d"));
    }

}
