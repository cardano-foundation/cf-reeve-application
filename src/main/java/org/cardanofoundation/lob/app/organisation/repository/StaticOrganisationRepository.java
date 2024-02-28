package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.cardanofoundation.lob.app.organisation.domain.core.ERPDataSource.NETSUITE;

@Component
@Slf4j
@RequiredArgsConstructor
public class StaticOrganisationRepository implements OrganisationRepository {

    private final OrganisationCurrencyRepository organisationCurrencyRepository;

    private List<Organisation> organisations = new ArrayList<>();

    @PostConstruct
    public void init() {

        organisations.add(new Organisation(
                Organisation.id("CHE-184.477.354"),
                "CF",
                "Cardano Foundation (CH)",
                NETSUITE,
                "1",
                "CHE-184.477.354",
                organisationCurrencyRepository.findByCurrencyId("ISO_4217:CHF").orElseThrow()
        ));

        log.info("StaticOrganisationRepository init completed.");
    }

    @Override
    public List<Organisation> listAll() {
        return List.copyOf(organisations);
    }

}
