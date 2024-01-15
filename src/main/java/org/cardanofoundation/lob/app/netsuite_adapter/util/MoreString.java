package org.cardanofoundation.lob.app.netsuite_adapter.util;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;

public final class MoreString {

    public static Optional<String> normaliseString(@Nullable String s) {
        if (StringUtils.isBlank(s)) {
            return Optional.empty();
        }

        return Optional.of(s);
    }

}
