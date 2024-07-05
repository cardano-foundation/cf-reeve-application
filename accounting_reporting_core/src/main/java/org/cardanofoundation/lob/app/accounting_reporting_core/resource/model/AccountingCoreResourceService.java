package org.cardanofoundation.lob.app.accounting_reporting_core.resource.model;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.lob.app.organisation.domain.entity.Organisation;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@org.springframework.stereotype.Service
@Slf4j
@Transactional(readOnly = true)
public class AccountingCoreResourceService {

    public boolean checkFromToDates(Organisation org,
                                    String dateFrom,
                                    String dateTo) {
        LocalDate dateFromObj = LocalDate.parse(dateFrom);
        LocalDate dateToObj = LocalDate.parse(dateTo);

        LocalDate today = LocalDate.now();
        LocalDate monthsAgo = today.minusMonths(org.getAccountPeriodMonths());
        LocalDate yesterday = today.minusDays(1);

        return dateFromObj.isAfter(monthsAgo) && dateToObj.isBefore(yesterday);
    }

}
