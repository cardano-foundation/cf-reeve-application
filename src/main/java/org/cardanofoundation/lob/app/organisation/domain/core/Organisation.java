package org.cardanofoundation.lob.app.organisation.domain.core;

import java.util.List;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsHex;

public record Organisation(
        // unique identifier for the organisation
        String id,
        String shortName,  // CF
        String longName, // Cardano Foundation
        List<ERPDataSource> ERPDataSources,
        String connectorId, // company id // application or entity id?
        String erpSystemCompanyId, // foreign system id, e.g. in case of NetSuite this is 1 for CF
        OrganisationCurrency currency
) {

    public static String testOrgId() {
        return "1ad9c73900dfb2f2a0d347af1968778ff6e8d4c17960014575dea994e4a9783c";
    }

    public static String id(
            ERPDataSource erpDataSource,
            String erpSystemConnectorId,
            String erpSystemCompanyId) {
        return digestAsHex(STR."\{erpDataSource.name()}::\{erpSystemConnectorId}::\{erpSystemCompanyId}");
    }

}
