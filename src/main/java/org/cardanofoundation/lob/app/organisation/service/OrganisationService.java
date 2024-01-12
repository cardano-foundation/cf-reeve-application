package org.cardanofoundation.lob.app.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.AccountSystemProvider;
import org.cardanofoundation.lob.app.organisation.domain.Organisation;
import org.cardanofoundation.lob.app.organisation.repository.OrganisactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisactionRepository organisactionRepository;

    @Transactional(readOnly = true)
    public Optional<Organisation> findById(String id) {
        return organisactionRepository.listAll().stream()
                .filter(organisation -> organisation.id().equals(id))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Organisation> findByForeignProvider(String foreignId,
                                                        AccountSystemProvider accountSystemProvider) {
        return organisactionRepository.listAll().stream()
                .filter(organisation -> organisation.accountSystemProviderId().equals(foreignId))
                .filter(organisation -> organisation.accountSystemProvider().equals(accountSystemProvider))
                .findFirst();
    }

}
