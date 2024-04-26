package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity.TransactionEntity;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;

import java.util.Optional;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OperationType.CREDIT;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.Journal;

@RequiredArgsConstructor
@Slf4j
public class JournalAccountCreditEnrichmentTaskItem implements PipelineTaskItem {

    private final OrganisationPublicApiIF organisationPublicApiIF;

    @Override
    public void run(TransactionEntity tx) {
        val dummyAccountM = organisationPublicApiIF.findByOrganisationId(tx.getOrganisation().getId())
                .flatMap(Organisation::getDummyAccount);

        if (!shouldTriggerNormalisation(tx, dummyAccountM)) {
            return;
        }

        log.info("Normalising journal transaction with id: {}", tx.getId());

        // at this point we can assume we have it, it is mandatory
        val dummyAccount = dummyAccountM.orElseThrow();
        for (val txItem : tx.getItems()) {
            val operationTypeM = txItem.getOperationType();

            if (operationTypeM.isEmpty()) {
                txItem.setAccountCodeCredit(dummyAccount);
                continue;
            }

            val operationType = operationTypeM.orElseThrow();

            if (txItem.getAccountCodeCredit().isEmpty() && operationType == CREDIT) {
                val accountCodeDebit = txItem.getAccountCodeDebit().orElseThrow();
                txItem.setAccountCodeCredit(accountCodeDebit);

                txItem.clearAccountCodeDebit();
            }

            if (txItem.getAccountCodeCredit().isEmpty()) {
                txItem.setAccountCodeCredit(dummyAccount);
            }
            if (txItem.getAccountCodeDebit().isEmpty()) {
                txItem.setAccountCodeDebit(dummyAccount);
            }
        }
    }

    private boolean shouldTriggerNormalisation(TransactionEntity tx,
                                               Optional<String> dummyAccountM) {
        return dummyAccountM.isPresent()
                && tx.getTransactionType() == Journal
                && tx.getItems().stream().allMatch(txItem -> txItem.getAccountCodeCredit().isEmpty());
    }

}
