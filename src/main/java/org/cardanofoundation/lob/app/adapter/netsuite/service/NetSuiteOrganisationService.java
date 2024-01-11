package org.cardanofoundation.lob.app.adapter.netsuite.service;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.adapter.netsuite.repository.NetSuiteOrganisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NetSuiteOrganisationService {

    private final NetSuiteOrganisationRepository netSuiteOrganisationRepository;

    @Transactional(readOnly = true)
    public Optional<String> findOrganisationName(Integer orgId) {
        return netSuiteOrganisationRepository.findOrganisationName(orgId);
    }

}
