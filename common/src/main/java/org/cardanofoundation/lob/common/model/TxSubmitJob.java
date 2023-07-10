package org.cardanofoundation.lob.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class TxSubmitJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private TxSubmitJobStatus jobStatus;

    private String transactionId;

    @Lob
    private byte[] transactionMetadata;
}
