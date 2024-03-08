package org.cardanofoundation.lob.app.netsuite_adapter.domain.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@AllArgsConstructor
@Getter
@ToString
public class Violation {

    private long subsidiary;

    private String transactionNumber;

    private int lineId;

    private Code code;

    private Map<String, Object> bag = Map.of();

    public enum Code {
        VAT_MAPPING_NOT_FOUND,
        CURRENCY_MAPPING_NOT_FOUND,
        ORGANISATION_MAPPING_NOT_FOUND,
        INVALID_TRANSACTION_LINE,
        CHART_OF_ACCOUNT_NOT_FOUND,
        TRANSACTION_TYPE_MAPPING_NOT_FOUND,
        COST_CENTER_NOT_FOUND,
        PROJECT_MAPPING_NOT_FOUND
    }

    public static Violation create(TxLine txLine, Code code, Map<String, Object> bag) {
        return new Violation(txLine.subsidiary(), txLine.transactionNumber(), txLine.lineID(), code, bag);
    }

    public static Violation create(TxLine txLine, Code code) {
        return new Violation(txLine.subsidiary(), txLine.transactionNumber(), txLine.lineID(), code, Map.of());
    }

}
