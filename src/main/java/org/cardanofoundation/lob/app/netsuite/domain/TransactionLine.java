package org.cardanofoundation.lob.app.netsuite.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.cardanofoundation.lob.app.netsuite.common.Constants.METADATA_DATE_PATTERN;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionLine {

    @JsonProperty("fingerprint")
    private String sourceEventFingerprint;
    private String entity;
    private String module;
    private String type;
    private String docCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = METADATA_DATE_PATTERN)
    private LocalDateTime docDate;
    private String period;
    private String vendor;
    private String costCenter;
    private String projectCode;
    private String currency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private String eventCode;
    private String eventDescription;
    private String lineNumber;
    private String Status;
    private Instant timestamp;

}
