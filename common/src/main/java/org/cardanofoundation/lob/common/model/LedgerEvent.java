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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.METADATA_DATE_PATTERN)
    private Date docDate;
    private String bookDate;
    private String transactionNumber;
    private String documentNumber;
    private String number;
    private String event;
    private String currency;
    private Double amountDebit;
    private Double amountCredit;
    private Double exchangeRate;
    private String counterParty;
    private String costCenter;
    private String projectCode;
    private String Status;
    private Long timestamp;
}
