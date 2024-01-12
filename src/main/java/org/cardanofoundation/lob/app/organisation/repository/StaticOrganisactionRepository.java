package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.Organisation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.cardanofoundation.lob.app.organisation.domain.AccountSystemProvider.NETSUITE;

@Service
@Slf4j
public class StaticOrganisactionRepository implements OrganisactionRepository {

    private final List<Organisation> organisations = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("StaticOrganisactionRepository init");

        organisations.add(new Organisation("CF", "CF", "Cardano Foundation", NETSUITE, "1", 0, Currency.getInstance("CHF")));
    }

    @Override
    public List<Organisation> listAll() {
        return List.copyOf(organisations);
    }

}
