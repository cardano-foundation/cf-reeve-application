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

    private final static String ORG_ID = "3Zs2g8qrcRxvlVzXQV0SdG2vNip9KOL6aoHs16P5Wgo=";

    @PostConstruct
    public void init() {
        organisations.add(new Organisation(
                ORG_ID,
                "CF",
                "Cardano Foundation (CH)",
                List.of(NETSUITE),
                "jhu765",
                "1",
                organisationCurrencyRepository.findByCurrencyId("ISO_4217:CHF").orElseThrow()
        ));

        log.info("StaticOrganisationRepository init completed.");
    }

    @Override
    public List<Organisation> listAll() {
        return List.copyOf(organisations);
    }

}
