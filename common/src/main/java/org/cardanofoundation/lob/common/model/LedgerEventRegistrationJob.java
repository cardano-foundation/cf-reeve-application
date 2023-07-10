package org.cardanofoundation.lob.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Entity
public class LedgerEventRegistrationJob {
    @Id
    private String registrationId;

    private LedgerEventRegistrationJobStatus jobStatus;

    @OneToMany(fetch = FetchType.EAGER)
    private List<LedgerEvent> ledgerEvents;
}
