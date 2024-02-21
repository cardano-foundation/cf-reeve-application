package org.cardanofoundation.lob.app.organisation.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.core.Organisation;
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

    private final static String ORG_ID = "3Zs2g8qrcRxvlVzXQV0SdG2vNip9KOL6aoHs16P5Wgo=";

    @PostConstruct
    public void init() {
        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "11",
                BigDecimal.valueOf(0.077D)
        ));
        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "7",
                BigDecimal.valueOf(0.077D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "8",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "5",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "181",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "181",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "159",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "12",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "9",
                BigDecimal.valueOf(3.7D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "6",
                BigDecimal.valueOf(2.5D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "10",
                BigDecimal.valueOf(2.5D)
        ));

        organisationVatList.add(new OrganisationVat(
                Organisation.testOrgId(),
                "18",
                BigDecimal.valueOf(100D)
        ));
    }

    @Override
    public Optional<OrganisationVat> findByOrganisationAndInternalNumber(String organisationId,
                                                                         String internalNumber) {
        return organisationVatList.stream()
                .filter(organisationVat -> organisationVat.organisationId().equals(organisationId))
                .filter(organisationVat -> organisationVat.internalId().equals(internalNumber))
                .findFirst();
    }

}
