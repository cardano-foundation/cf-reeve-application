package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.OrganisationTransactions;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.ValidationStatus.FAILED;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_SANITY_CHECK_FAIL;

@RequiredArgsConstructor
@Slf4j
public class LastSanityCheckProcessor implements PipelineTask {

    private final Validator validator;

    @Override
    public TransformationResult run(OrganisationTransactions passedTransactions,
                                    OrganisationTransactions ignoredTransactions) {
        val transactionsWithPossibleViolation = passedTransactions.transactions()
                .stream()
                .map(Transaction.WithPossibleViolations::create)
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

    private Transaction.WithPossibleViolations sanityCheckFields(Transaction.WithPossibleViolations withPossibleViolations) {
        val transaction = withPossibleViolations.transaction();
        val violations = new HashSet<Violation>();

        val errors = validator.validate(transaction);

        val alreadyFailed = withPossibleViolations.violations()
                .stream()
                .anyMatch(v -> v.transactionId().equals(transaction.getId()));

        if (!errors.isEmpty() && !alreadyFailed) {
            val v = Violation.create(
                    Violation.Priority.NORMAL,
                    Violation.Type.FATAL,
                    transaction.getOrganisation().getId(),
                    transaction.getId(),
                    TX_SANITY_CHECK_FAIL,
                    Map.of("errors", errors)
            );

            violations.add(v);
        }

        if (!violations.isEmpty()) {
            return Transaction.WithPossibleViolations
                    .create(transaction.toBuilder().validationStatus(FAILED).build(), violations);
        }

        return withPossibleViolations;
    }

}
