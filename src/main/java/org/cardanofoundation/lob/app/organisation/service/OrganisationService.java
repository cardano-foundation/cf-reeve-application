package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.AccountSystemProvider;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.cardanofoundation.lob.app.organisation.repository.OrganisationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    //@Transactional(readOnly = true)
    public final List<Organisation> listAll() {
        return organisationRepository.listAll();
    }

    //@Transactional(readOnly = true)
    public Optional<Organisation> findById(String id) {
        return organisationRepository.listAll().stream()
                .filter(organisation -> organisation.id().equals(id))
                .findFirst();
    }

    //@Transactional(readOnly = true)
    public Optional<Organisation> findByForeignProvider(String foreignId,
                                                        AccountSystemProvider accountSystemProvider) {
        return organisationRepository.listAll().stream()
                .filter(organisation -> organisation.accountSystemProviderId().equals(foreignId))
                .filter(organisation -> organisation.accountSystemProvider().equals(accountSystemProvider))
                .findFirst();
    }

}
