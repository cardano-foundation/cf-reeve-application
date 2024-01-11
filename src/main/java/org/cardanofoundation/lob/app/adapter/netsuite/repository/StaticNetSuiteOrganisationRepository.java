package org.cardanofoundation.lob.app.adapter.netsuite.repository;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StaticNetSuiteOrganisationRepository implements NetSuiteOrganisationRepository {

    @Override
    public Optional<String> findOrganisationName(Integer orgId) {
        return switch (orgId) {
            case 1 -> Optional.of("Cardano Foundation");
            default -> Optional.empty();
        };
    }

}
