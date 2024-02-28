package org.cardanofoundation.lob.app.organisation.domain.core;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsHex;

public record Organisation(
        String id, // unique identifier for the organisation
        String shortName,  // CF
        String longName, // Cardano Foundation
        ERPDataSource erpDataSource,
        String erpSystemCompanyId, // foreign system id, e.g. in case of NetSuite this is 1 for CF
        String vatID, // e.g. CHE-184.477.354
        OrganisationCurrency currency
) {

    public static String testOrgId() {
        return Organisation.id("CHE-184.477.354");
    }

    public static String id(String vatNumber) {
        return digestAsHex(vatNumber);
    }

}
