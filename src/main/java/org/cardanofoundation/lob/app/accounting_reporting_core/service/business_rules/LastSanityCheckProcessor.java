package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_SANITY_CHECK_FAIL;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Type.ERROR;

@RequiredArgsConstructor
@Slf4j
public class LastSanityCheckProcessor implements PipelineTask {

    private final Validator validator;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions,
                                    Set<Violation> allViolationUntilNow) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(tx -> TransactionWithViolations.create(tx, allViolationUntilNow))
                .map(this::sanityCheckFields)
                .collect(Collectors.toSet());

        val newViolations = new HashSet<Violation>();

        val finalTransactions = new HashSet<Transaction>();

        for (val violationTransaction : transactionsWithPossibleViolation) {
            finalTransactions.add(violationTransaction.transaction());
            newViolations.addAll(violationTransaction.violations());
        }

        return new TransformationResult(
                new OrganisationTransactions(passedTransactions.organisationId(), finalTransactions),
                ignoredTransactions,
                newViolations
        );
    }

    private TransactionWithViolations sanityCheckFields(TransactionWithViolations withPossibleViolations) {
        val tx = withPossibleViolations.transaction();
        val violations = new HashSet<Violation>();

        val errors = validator.validate(tx);

        val notFailedYet = withPossibleViolations.violations()
                .stream()
                .noneMatch(v -> v.transactionId().equals(tx.getId()));

        if (!errors.isEmpty() && notFailedYet) {
            val v = Violation.create(
                    ERROR,
                    tx.getOrganisation().getId(),
                    tx.getId(),
                    TX_SANITY_CHECK_FAIL,
                    ConversionsPipelineTask.class.getName(),
                    Map.of(
                            "transactionNumber", tx.getInternalTransactionNumber(),
                            "errors", errors
                    )
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return TransactionWithViolations
                    .create(tx.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

}
