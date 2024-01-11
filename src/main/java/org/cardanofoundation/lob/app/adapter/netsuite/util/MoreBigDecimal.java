package org.cardanofoundation.lob.app.adapter.netsuite.util;

import io.micrometer.common.lang.Nullable;

import java.math.BigDecimal;
import java.util.Optional;

public class MoreBigDecimal {

    @Nullable
    public static BigDecimal substract(@Nullable BigDecimal a, @Nullable BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }

        return a.subtract(b);
    }

    public static Optional<BigDecimal> substractOpt(@Nullable BigDecimal a, @Nullable BigDecimal b) {
        return Optional.ofNullable(substract(a, b));
    }

}
