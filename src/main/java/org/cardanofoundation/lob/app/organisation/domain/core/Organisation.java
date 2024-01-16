package org.cardanofoundation.lob.app.organisation.domain.core;

public record Organisation(String id,
                           String shortName,
                           String longName,
                           AccountSystemProvider accountSystemProvider,
                           String accountSystemProviderId, // foreign system account id
                           OrganisationCurrency baseCurrency
                           ) {
}
