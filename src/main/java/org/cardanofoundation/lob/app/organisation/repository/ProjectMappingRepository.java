package org.cardanofoundation.lob.app.organisation.repository;

import org.cardanofoundation.lob.app.organisation.domain.core.ProjectMapping;

import java.util.Optional;

public interface ProjectMappingRepository {

    Optional<ProjectMapping> getProject(String organisationId, String internalNumber);

}
