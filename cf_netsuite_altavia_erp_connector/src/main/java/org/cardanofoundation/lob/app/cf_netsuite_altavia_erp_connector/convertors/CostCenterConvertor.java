package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors;

import io.vavr.control.Either;
import lombok.val;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import java.util.function.Function;
import java.util.regex.Pattern;

public class CostCenterConvertor implements Function<String, Either<Problem, String>> {

    private static final Pattern pattern = Pattern.compile("(\\d{4}) (.+)");

    @Override
    public Either<Problem, String> apply(String s) {
        val matcher = pattern.matcher(s);

        if (matcher.groupCount() == 2) {
            if (matcher.find()) {
                return Either.right(matcher.group(1));
            }
        }

        return Either.left(Problem.builder()
                .withTitle("INVALID_COST_CENTER")
                .withDetail("Invalid cost center")
                .with("cost_center", s)
                .build());

    }

}
