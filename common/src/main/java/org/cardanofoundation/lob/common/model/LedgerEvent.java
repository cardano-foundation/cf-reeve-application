package org.cardanofoundation.lob.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class LedgerEvent {
    @Id
    private String sourceEventFingerprint;
    private String entity;
    private String module;
    private String type;
    private Date docDate;
    private String bookDate;
    private String transactionNumber;
    private String documentNumber;
    private String event;
    private String currency;
    private Double amount;
    private Double exchangeRate;
    private String counterParty;
    private String costCenter;
    private String projectCode;
    private Date timestamp;
}
