package org.cardanofoundation.lob.app.adapter.netsuite.repository;

import java.util.Optional;

public interface NetSuiteOrganisationRepository {

    Optional<String> findOrganisationName(Integer orgId);

}
