package org.cardanofoundation.lob.app.organisation.domain.core;

import java.math.BigDecimal;

public record OrganisationVat(
        String organisationId,
        String internalId,
        BigDecimal rate
        ) {

}
