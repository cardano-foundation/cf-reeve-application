package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors;

import io.vavr.control.Either;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.zalando.problem.Problem;

import java.util.function.Function;
import java.util.regex.Pattern;

public class AccountNumberConvertor implements Function<String, Either<ProblemDetail, String>> {

    private static final Pattern pattern = Pattern.compile("^(\\d+)");

    @Override
    public Either<ProblemDetail, String> apply(String s) {
        val matcher = pattern.matcher(s);

        if (matcher.find()) {
            return Either.right(matcher.group(1));
        }
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid account number");
        problemDetail.setTitle("INVALID_ACCOUNT_NUMBER");
        problemDetail.setProperty("account_number", s);
        return Either.left(problemDetail);
    }

}
