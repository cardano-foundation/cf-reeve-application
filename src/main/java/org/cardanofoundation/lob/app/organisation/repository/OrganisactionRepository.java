package org.cardanofoundation.lob.app.organisation.repository;


import org.cardanofoundation.lob.app.organisation.domain.Organisation;

import java.util.List;

public interface OrganisactionRepository {

    List<Organisation> listAll();

}
