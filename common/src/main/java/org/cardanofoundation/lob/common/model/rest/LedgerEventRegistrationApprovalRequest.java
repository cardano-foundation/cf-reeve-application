package org.cardanofoundation.lob.common.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LedgerEventRegistrationApprovalRequest {
    private String registrationId;
}
