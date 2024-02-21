package org.cardanofoundation.lob.app.organisation.domain.core;

import java.util.List;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsBase64;

public record Organisation(
        // unique identifier for the organisation
        String id,
        String shortName,  // CF
        String longName, // Cardano Foundation
        List<ERPDataSource> ERPDataSources,
        String connectorId, // company id // application or entity id?
        String accountSystemProviderId, // foreign system id, e.g. in case of NetSuite this is 1 for CF
        OrganisationCurrency currency
) {

    public static String testOrgId() {
        return "dd9b3683caab711c6f955cd7415d12746daf362a7d28e2fa6a81ecd7a3f95a0a";
    }

    // TODO this needs to be thought through
    public static String id(
            String connectorId,
            String foreignSystemId) {
        return digestAsBase64(STR."\{connectorId}::\{foreignSystemId}");
    }

    // TODO refactor application to use concept of connectors
    // we can connect to multiple systems, e.g. NetSuite, QuickBooks, etc. for one organisation
    public record Connector() {

    }

}
