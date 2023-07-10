package org.cardanofoundation.lob.common.model.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.lob.common.model.LedgerEventRegistrationJobStatus;

@Data
@NoArgsConstructor
public class LedgerEventRegistrationApprovalResponse {
    private String registrationId;
    private LedgerEventRegistrationJobStatus jobStatus;
}
