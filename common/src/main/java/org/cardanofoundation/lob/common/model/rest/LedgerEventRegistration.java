package org.cardanofoundation.lob.common.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.common.model.LedgerEvent;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEventRegistration {
    private String registrationId;
    private List<LedgerEvent> ledgerEvents;
}
