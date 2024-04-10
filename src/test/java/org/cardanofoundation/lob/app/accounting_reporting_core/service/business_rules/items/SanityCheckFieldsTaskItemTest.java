package org.cardanofoundation.lob.app.accounting_reporting_core.service.business_rules.items;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Currency;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Organisation;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.CoreCurrency.IsoStandard.ISO_4217;
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
        val tx = mock(Transaction.class);
        when(validator.validate(tx)).thenReturn(Collections.emptySet());

        val result = taskItem.run(tx);

        assertThat(result.getViolations()).isEmpty();
        verify(validator, times(1)).validate(tx);
    }

    @Test
    void testTransactionFailsSanityCheck() {
        // Assuming CoreCurrency and other related objects are correctly instantiated
        val coreCurrency = new CoreCurrency(
                ISO_4217,
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

        val result = taskItem.run(transaction);

        assertThat(result.getViolations()).isNotEmpty();
        assertThat(result.getViolations().iterator().next().code()).isEqualTo(TX_SANITY_CHECK_FAIL);
    }

}
