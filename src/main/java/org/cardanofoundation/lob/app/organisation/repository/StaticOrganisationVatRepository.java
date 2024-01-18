package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.OrganisationVat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StaticOrganisationVatRepository implements OrganisationVatRepository {

    private List<OrganisationVat> organisationVatList = new ArrayList<>();

    @PostConstruct
    public void init() {
        organisationVatList.add(new OrganisationVat(
                "CF",
                "11",
                BigDecimal.valueOf(0.077D)
        ));
        organisationVatList.add(new OrganisationVat(
                "CF",
                "7",
                BigDecimal.valueOf(0.077D)
        ));

        organisationVatList.add(new OrganisationVat(
                "CF",
                "8",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "CF",
                "5",
                BigDecimal.valueOf(0.0D)
        ));

        // TODO rest of mapping codes goes here
    }

    @Override
    public Optional<OrganisationVat> findByInternalId(String internalId) {
        return organisationVatList.stream()
                .filter(organisationVat -> organisationVat.internalId().equals(internalId))
                .findFirst();
    }

}
