package org.cardanofoundation.lob.app.organisation.repository;


import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;

import java.util.List;

public interface OrganisationRepository {

    List<Organisation> listAll();

}
