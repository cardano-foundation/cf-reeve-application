package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.PipelineTask;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items.PipelineTaskItem;

import java.util.List;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.VALIDATED;

@RequiredArgsConstructor
@Slf4j
@Setter
public class DefaultPipelineTask implements PipelineTask {

    private final List<PipelineTaskItem> items;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions) {
        if (passedTransactions.transactions().isEmpty()) {
            return new TransformationResult(passedTransactions, ignoredTransactions);
        }

        for (val transactionEntity : passedTransactions.transactions()) {
            runTaskItems(transactionEntity);
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), passedTransactions.transactions()),
                ignoredTransactions
        );
    }

    private void runTaskItems(TransactionEntity transaction) {
        for (val taskItem : items) {
            taskItem.run(transaction);
        }
    }

}
