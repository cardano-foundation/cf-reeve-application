package org.cardanofoundation.lob.app.organisation.repository;


import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationVat;

import java.util.Optional;

public interface OrganisationVatRepository {

    Optional<OrganisationVat> findByInternalId(String organisationId);

}
