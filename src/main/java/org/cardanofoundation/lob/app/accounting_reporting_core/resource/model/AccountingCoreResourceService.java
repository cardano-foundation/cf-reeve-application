package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApi;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingCoreResourceService {

    private final OrganisationPublicApi organisationPublicApi;

    private Organisation organisation;

    public boolean findOrganizationById(String organisationId) {
        this.organisation = organisationPublicApi.findByOrganisationId(organisationId).stream().findFirst().orElse(null);
        if (null == this.organisation) {
            return false;
        }
        return true;
    }

    public boolean checkFromToDates(String dateFrom, String dateTo) {
        LocalDate dateFromObj = LocalDate.parse(dateFrom);
        LocalDate dateToObj = LocalDate.parse(dateTo);

        if (null == this.organisation) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate monthsAgo = today.minusMonths(this.organisation.getAccountPeriodMonths());
        LocalDate yesterday = today.minusDays(1);

        return dateFromObj.isAfter(monthsAgo) && dateToObj.isBefore(yesterday);
    }

}
