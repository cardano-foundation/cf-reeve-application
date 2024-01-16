package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    @Transactional(readOnly = true)
    public Optional<Organisation> findById(String id) {
        return organisationRepository.listAll().stream()
                .filter(organisation -> organisation.id().equals(id))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Organisation> findByForeignProvider(String foreignId,
                                                        AccountSystemProvider accountSystemProvider) {
        return organisationRepository.listAll().stream()
                .filter(organisation -> organisation.accountSystemProviderId().equals(foreignId))
                .filter(organisation -> organisation.accountSystemProvider().equals(accountSystemProvider))
                .findFirst();
    }

}
