package org.cardanofoundation.lob.app.organisation;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.Organisation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrganisationService {

    public List<Organisation> findAll() {
        return List.of(
                new Organisation("1", "Organisation 1"),
                new Organisation("2", "Organisation 2"),
                new Organisation("3", "Organisation 3")
        );
    }

}
