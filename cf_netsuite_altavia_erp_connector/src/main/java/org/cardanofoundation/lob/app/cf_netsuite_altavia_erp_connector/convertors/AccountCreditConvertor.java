package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors;

import io.vavr.control.Either;
import lombok.val;
import org.zalando.problem.Problem;

import java.util.function.Function;
import java.util.regex.Pattern;

public class AccountCreditConvertor implements Function<String, Either<Problem, String>> {

    private static final Pattern pattern = Pattern.compile("^(\\d+)\\s(.*)$");

    @Override
    public Either<Problem, String> apply(String s) {
        val matcher = pattern.matcher(s);

        if (matcher.matches()) {
            return Either.right(STR."\{matcher.group(2)}");
        }

        return Either.left(Problem.builder()
                .withTitle("INVALID_CREDIT_ACCOUNT")
                .withDetail("Invalid credit account code")
                .with("project", s)
                .build());
    }

}
