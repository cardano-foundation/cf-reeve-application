package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Violation.Code.TX_SANITY_CHECK_FAIL;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SanityCheckFieldsTaskItemTest {

    @Mock
    private Validator validator;

    private SanityCheckFieldsTaskItem taskItem;

    @BeforeEach
    public void setUp() {
        taskItem = new SanityCheckFieldsTaskItem(validator);
    }

    @Test
    void testTransactionPassesSanityCheck() {
        Transaction transaction = mock(Transaction.class);
        when(validator.validate(transaction)).thenReturn(Collections.emptySet());

        TransactionWithViolations txWithViolations = new TransactionWithViolations(transaction, new HashSet<>());

        TransactionWithViolations result = taskItem.run(txWithViolations);

        assertThat(result.violations()).isEmpty();
        verify(validator, times(1)).validate(transaction);
    }

    @Test
    void testTransactionFailsSanityCheck() {
        // Assuming CoreCurrency and other related objects are correctly instantiated
        val coreCurrency = new CoreCurrency(
                CoreCurrency.IsoStandard.ISO_4217,
                "USD",
                Optional.of("840"), // ISO 4217 code for US Dollar
                "US Dollar"
        );
        val currency = Currency.builder()
                .customerCode("USD")
                .coreCurrency(Optional.of(coreCurrency))
                .build();
        val organisation = new Organisation("org1", Optional.of("Org Name"), Optional.of(currency));

        val transaction = Transaction.builder()
                .organisation(organisation)
                .internalTransactionNumber("1")
                .build();

        // Mocking the violation returned by the validator
        val violation = mock(ConstraintViolation.class);
        val violations = new HashSet<ConstraintViolation<Transaction>>();
        violations.add(violation);

        when(validator.validate(transaction)).thenReturn(violations);

        val txWithViolations = new TransactionWithViolations(transaction, new HashSet<>());

        val result = taskItem.run(txWithViolations);

        assertThat(result.violations()).isNotEmpty();
        assertThat(result.violations().iterator().next().code()).isEqualTo(TX_SANITY_CHECK_FAIL);
    }

}
