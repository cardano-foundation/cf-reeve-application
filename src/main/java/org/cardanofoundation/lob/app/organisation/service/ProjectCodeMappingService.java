package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.ProjectMapping;
import org.cardanofoundation.lob.app.organisation.repository.ProjectMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectCodeMappingService {

    private final ProjectMappingRepository projectMappingRepository;

    public Optional<ProjectMapping> getProject(String organisationId, String internalNumber){
        return projectMappingRepository.getProject(organisationId, internalNumber);
    }

}
