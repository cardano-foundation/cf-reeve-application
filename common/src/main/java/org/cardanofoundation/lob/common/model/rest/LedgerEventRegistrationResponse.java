package org.cardanofoundation.lob.common.model.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.common.model.LedgerEvent;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEventRegistrationResponse {
    private String registrationId;
    private LedgerEventRegistrationJobStatus jobStatus;
}
