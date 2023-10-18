package org.cardanofoundation.lob.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.common.constants.Constants;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class LedgerEvent {
    @Id
    @JsonProperty("fingerprint")
    private String sourceEventFingerprint;
    private String entity;
    private String module;
    private String type;
    private String docCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.METADATA_DATE_PATTERN)
    private Date docDate;
    private String period;
    private String vendor;
    private String costCenter;
    private String projectCode;
    private String currency;
    /**
     * @// TODO: 18/10/2023 If is float, we should use Double/Float/String? to not loose anything 
     */
    private String amount;
    private String exchangeRate;
    private String eventCode;
    private String eventDescription;
    private String lineNumber;
    private String Status;
    private Long timestamp;
}
