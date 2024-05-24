package org.cardanofoundation.lob.app.accounting_reporting_core.resource.views;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionBatchStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.BatchStatistics;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Rename the class name BatchView
 */
@Getter
@Setter
@AllArgsConstructor
public class BatchView {
    private String id;
    private String createdAt;
    private String updatedAt;
    private String organisationId;
    private TransactionBatchStatus status;
    private Optional<BatchStatistics> batchStatistics;
    private Set<TransactionView> transactions = new LinkedHashSet<>();

}
