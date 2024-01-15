package org.cardanofoundation.lob.app.organisation.domain.core;

import java.util.Currency;

public record Organisation(String id,
                           String shortName,
                           String longName,
                           AccountSystemProvider accountSystemProvider,
                           String accountSystemProviderId, // foreign system account id
                           int accountSystemProviderIntegrationVersion, // version of the integration with the foreign system
                           Currency baseCurrency
                           ) {
}
