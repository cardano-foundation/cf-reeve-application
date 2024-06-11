package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class FatalError {

    private final ErrorCode code;

    private final Map<String, Object> bag;

    public enum ErrorCode {
        INTERNAL,
        ORGANISATION_NOT_IMPORTED,
        TRANSACTION_TYPE_NOT_YET_KNOWN,
    }

}
