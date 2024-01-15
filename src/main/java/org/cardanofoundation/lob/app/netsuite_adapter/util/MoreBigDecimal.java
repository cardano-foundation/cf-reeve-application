package org.cardanofoundation.lob.app.netsuite_adapter.util;

import io.micrometer.common.lang.Nullable;

import java.math.BigDecimal;
import java.util.Optional;

public class MoreBigDecimal {

    public static BigDecimal zeroForNull(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    @Nullable
    public static BigDecimal substract(@Nullable BigDecimal a, @Nullable BigDecimal b) {
        return a.subtract(b);
    }

    public static Optional<BigDecimal> substractOpt(@Nullable BigDecimal a, @Nullable BigDecimal b) {
        return Optional.ofNullable(substract(a, b));
    }

}
