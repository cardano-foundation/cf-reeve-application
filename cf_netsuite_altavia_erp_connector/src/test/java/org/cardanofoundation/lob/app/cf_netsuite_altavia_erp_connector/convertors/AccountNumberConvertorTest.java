package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountNumberConvertorTest {

    private AccountNumberConvertor accountNumberConvertor;

    @BeforeEach
    public void setUp() {
        accountNumberConvertor = new AccountNumberConvertor();
    }

    @Test
    public void testExtractAccountNumber_validAccountNumber() {
        String input = "2101110100 Accounts Payable : Accounts Payable CHF";
        Either<Problem, String> result = accountNumberConvertor.apply(input);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo("2101110100");
    }

    @Test
    public void testExtractAccountNumber_validAccountNumberWithNewLine() {
        String input = "2102110100 UBS Corporate Credit Cards\n0000000000";
        Either<Problem, String> result = accountNumberConvertor.apply(input);

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo("2102110100");
    }

    @Test
    public void testExtractAccountNumber_noAccountNumber() {
        String input = "No account number at the beginning";
        Either<Problem, String> result = accountNumberConvertor.apply(input);

        assertThat(result.getLeft()).isNotNull();
        assertThat(result.getLeft().getTitle()).isEqualTo("INVALID_ACCOUNT_NUMBER");
    }

    @Test
    public void testExtractAccountNumber_emptyString() {
        String input = "";
        Either<Problem, String> result = accountNumberConvertor.apply(input);

        assertThat(result.getLeft()).isNotNull();
    }

}