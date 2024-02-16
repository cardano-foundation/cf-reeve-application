package org.cardanofoundation.lob.app.organisation.domain.core;

import java.util.List;

import static org.cardanofoundation.lob.app.support.crypto_support.SHA3.digestAsBase64;

public record Organisation(
        // unique identifier for the organisation
        String id, // GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=
        String shortName,  // CF
        String longName, // Cardano Foundation
        List<AccountSystemProvider> accountSystemProviders,
        String connectorId, // company id // application or entity id?
        String accountSystemProviderId, // foreign system id, e.g. in case of NetSuite this is 1 for CF
        OrganisationCurrency baseCurrency
) {

    public static String id(AccountSystemProvider accountSystemProvider,
                            String connectorId,
                            String foreignSystemId) {
        return digestAsBase64(STR."\{accountSystemProvider.name()}::\{connectorId}::\{foreignSystemId}");
    }

    // TODO refactor application to use concept of connectors
    // we can connect to multiple systems, e.g. NetSuite, QuickBooks, etc. for one organisation
    public record Connector() {

    }

}
