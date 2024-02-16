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
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "11",
                BigDecimal.valueOf(0.077D)
        ));
        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "7",
                BigDecimal.valueOf(0.077D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "8",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "5",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "181",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "181",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "159",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "12",
                BigDecimal.valueOf(0.0D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "9",
                BigDecimal.valueOf(3.7D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "6",
                BigDecimal.valueOf(2.5D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
                "10",
                BigDecimal.valueOf(2.5D)
        ));

        organisationVatList.add(new OrganisationVat(
                "GtnHOQDfsvKg00evGWh3j/bo1MF5YAFFdd6plOSpeDw=",
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
